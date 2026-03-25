#!/usr/bin/env node

const origin = process.argv[2] ?? 'http://localhost:8080';
const rootApi = new URL('/api/accounting', origin).toString();
const treeApi = new URL('/api/accounting/agent-tree', origin).toString();

const aiProvidedAgentPlans = [
  {
    providedBy: 'ai',
    objective: 'record a sales settlement from the customer context',
    plan: [
      { rel: 'customer', action: 'follow-link' },
      { rel: 'source-evidences', action: 'submit-template' },
    ],
  },
  {
    providedBy: 'ai',
    objective: 'inspect the source evidence behind the first posted transaction',
    plan: [
      { rel: 'customer', action: 'follow-link' },
      { rel: 'account', action: 'follow-link' },
      { rel: 'transaction', action: 'follow-link' },
      { rel: 'source-evidence', action: 'follow-link' },
    ],
  },
  {
    providedBy: 'ai',
    objective: 'pivot from source evidence back to the posting account',
    plan: [
      { rel: 'customer', action: 'follow-link' },
      { rel: 'source-evidence', action: 'follow-link' },
      { rel: 'transaction', action: 'follow-link' },
      { rel: 'account', action: 'follow-link' },
    ],
  },
];

async function main() {
  const tree = await requestJson('GET', treeApi);
  console.log(`Loaded accounting root ${rootApi}`);
  console.log(`Loaded agent tree ${treeApi}`);

  for (const aiPlan of aiProvidedAgentPlans) {
    console.log(`\n=== ${aiPlan.objective} ===`);
    console.log(`AI plan: ${JSON.stringify(aiPlan.plan)}`);

    const trace = [];
    const result = await runAiPlan(aiPlan, tree, trace);

    console.log('Trace:');
    for (const entry of trace) {
      console.log(`- ${entry}`);
    }
    console.log('Final resource:');
    console.log(JSON.stringify(summarizeResource(result), null, 2));
  }
}

async function runAiPlan(aiPlan, tree, trace) {
  let resource = await requestJson('GET', rootApi);
  let treeNode = tree;

  trace.push(`AI provided ${aiPlan.plan.length} rel steps`);

  for (const step of aiPlan.plan) {
    const nextTreeNode = requireTreeLink(treeNode, step.rel);
    trace.push(`Resolved rel "${step.rel}" in agent-tree -> ${nextTreeNode.api}`);

    resource = await executeStep(resource, nextTreeNode, step, trace);
    treeNode = nextTreeNode;
  }

  return resource;
}

async function executeStep(resource, treeNode, step, trace) {
  if (step.action === 'submit-template') {
    const template = requireTemplate(resource, step.rel, treeNode.api);
    const request = materializeRequest(template, treeNode.api);
    trace.push(
        `Submitting template "${step.rel}" via ${request.method} ${request.url} body=${JSON.stringify(request.body)}`);
    return requestJson(request.method, request.url, request.body);
  }

  const link = requireLink(resource, step.rel, treeNode.api);
  const url = absoluteUrl(link.href);
  trace.push(`Following link "${step.rel}" -> ${url}`);
  return requestJson('GET', url);
}

function requireTreeLink(treeNode, rel) {
  const link = (treeNode.links ?? []).find((candidate) => candidate.rel === rel);
  if (!link) {
    throw new Error(`Rel "${rel}" not found in agent-tree node ${treeNode.api}`);
  }
  return link;
}

function requireLink(resource, rel, expectedHref) {
  const direct = resource?._links?.[rel];
  if (direct) {
    return Array.isArray(direct) ? direct[0] : direct;
  }

  const fallback = findLinkByHref(resource, expectedHref);
  if (!fallback) {
    throw new Error(`Link "${rel}" not exposed on current resource`);
  }
  return fallback;
}

function requireTemplate(resource, rel, expectedTarget) {
  const direct = resource?._templates?.[rel];
  if (direct) {
    return direct;
  }

  const fallback = findTemplateByTarget(resource, expectedTarget);
  if (!fallback) {
    throw new Error(`Template "${rel}" not exposed on current resource`);
  }
  return fallback;
}

function findLinkByHref(resource, href) {
  const expected = absoluteUrl(href);
  for (const value of Object.values(resource?._links ?? {})) {
    const candidates = Array.isArray(value) ? value : [value];
    const matched = candidates.find((candidate) => absoluteUrl(candidate.href) === expected);
    if (matched) {
      return matched;
    }
  }
  return null;
}

function findTemplateByTarget(resource, href) {
  const expected = absoluteUrl(href);
  for (const template of Object.values(resource?._templates ?? {})) {
    if (template?.target && absoluteUrl(template.target) === expected) {
      return template;
    }
  }
  return null;
}

function materializeRequest(template, fallbackTarget) {
  const method = (template.method ?? 'POST').toUpperCase();
  const url = absoluteUrl(template.target ?? fallbackTarget);
  const body = {};

  for (const property of template.properties ?? []) {
    body[property.name] = choosePropertyValue(property);
  }

  return { method, url, body };
}

function choosePropertyValue(property) {
  if (property.value !== undefined && property.value !== null) {
    return property.value;
  }

  if (property.name === 'accountId') {
    return firstOptionValue(property) ?? 'CASH-001';
  }
  if (property.name === 'detailAmounts') {
    return ['680.00', '320.00'];
  }
  if (property.name === 'orderId') {
    return 'ORDER-AI-PLAN-1001';
  }

  return `${property.name ?? 'field'}-from-ai-template`;
}

function firstOptionValue(property) {
  const inline = property?.options?.inline;
  if (!Array.isArray(inline) || inline.length === 0) {
    return null;
  }
  return inline[0]?.value ?? null;
}

function summarizeResource(resource) {
  return {
    type: inferResourceType(resource),
    id: resource.id ?? resource.operatorId ?? resource.customerId ?? null,
    name: resource.name ?? resource.customerName ?? resource.operatorName ?? null,
    links: Object.keys(resource?._links ?? {}),
    templates: Object.keys(resource?._templates ?? {}),
  };
}

function inferResourceType(resource) {
  if (resource.transactions) {
    return 'account-or-source-evidence';
  }
  if (resource.current) {
    return 'account';
  }
  if (resource.orderId) {
    return 'source-evidence';
  }
  if (resource.amount) {
    return 'transaction';
  }
  if (resource.email) {
    return 'customer';
  }
  if (resource.customerName) {
    return 'accounting-root';
  }
  return 'resource';
}

function absoluteUrl(href) {
  return new URL(href, origin).toString();
}

async function requestJson(method, url, body) {
  console.log(`${method} ${url}`);

  const response = await fetch(url, {
    method,
    headers: {
      accept: 'application/prs.hal-forms+json, application/hal+json, application/json',
      ...(body ? { 'content-type': 'application/json' } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  if (!response.ok) {
    throw new Error(`${method} ${url} failed: ${response.status} ${text}`);
  }
  return text ? JSON.parse(text) : {};
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
