#!/usr/bin/env node

const origin = process.argv[2] ?? 'http://localhost:8080';
const treeUrl = new URL('/api/ecommerce/agent-tree', origin).toString();

const agentPlans = [
  {
    name: 'seller creates a listing',
    path: {
      rel: 'seller-store',
      next: {
        rel: 'create-listing',
      },
    },
  },
  {
    name: 'buyer creates a purchase',
    path: {
      rel: 'buyer-account',
      next: {
        rel: 'create-purchase',
      },
    },
  },
];

async function main() {
  const tree = await requestJson('GET', treeUrl);
  console.log(`Loaded agent tree from ${treeUrl}`);

  for (const plan of agentPlans) {
    console.log(`\n=== ${plan.name} ===`);
    const rootResource = await requestJson('GET', toUrl(tree.api));
    const result = await executePath(tree, rootResource, plan.path);
    console.log(JSON.stringify(result, null, 2));
  }
}

async function executePath(treeNode, resource, step) {
  const childNode = findLink(treeNode, step.rel);
  const nextResource = hasTemplateFor(resource, step.rel, childNode.api)
      ? await invokeTemplate(resource, childNode, step.rel)
      : await followLink(resource, step.rel);
  if (!step.next) {
    return nextResource;
  }
  return executePath(childNode, nextResource, step.next);
}

function findLink(treeNode, rel) {
  const link = (treeNode.links ?? []).find((candidate) => candidate.rel === rel);
  if (!link) {
    throw new Error(`Rel not found in agent tree: ${rel}`);
  }
  return link;
}

async function followLink(resource, rel) {
  const link = getRequiredLink(resource, rel);
  return requestJson('GET', toUrl(link.href));
}

async function invokeTemplate(resource, treeNode, rel) {
  const template = getRequiredTemplate(resource, rel, treeNode.api);
  const targetHref = template.target ?? treeNode.api;
  const backingLink = findLinkByHref(resource, targetHref);
  if (!backingLink) {
    throw new Error(`Template target is not exposed as a link: ${rel} -> ${targetHref}`);
  }

  const body = buildBodyFromTemplate(template);
  const method = (template.method ?? 'POST').toUpperCase();
  return requestJson(method, toUrl(targetHref), body);
}

function getRequiredLink(resource, rel) {
  const candidate = resource?._links?.[rel];
  if (!candidate) {
    throw new Error(`Link not found on resource: ${rel}`);
  }
  return Array.isArray(candidate) ? candidate[0] : candidate;
}

function getRequiredTemplate(resource, rel, api) {
  const template = resource?._templates?.[rel];
  if (!template) {
    const fallback = findTemplateByTarget(resource, api);
    if (!fallback) {
      throw new Error(`Template not found on resource: ${rel}`);
    }
    return fallback;
  }
  return template;
}

function hasTemplateFor(resource, rel, api) {
  return Boolean(resource?._templates?.[rel] ?? findTemplateByTarget(resource, api));
}

function findLinkByHref(resource, href) {
  const expected = toUrl(href);
  for (const value of Object.values(resource?._links ?? {})) {
    const candidates = Array.isArray(value) ? value : [value];
    const matched = candidates.find((candidate) => toUrl(candidate.href) === expected);
    if (matched) {
      return matched;
    }
  }
  return null;
}

function findTemplateByTarget(resource, href) {
  const expected = toUrl(href);
  for (const template of Object.values(resource?._templates ?? {})) {
    if (template?.target && toUrl(template.target) === expected) {
      return template;
    }
  }
  return null;
}

function buildBodyFromTemplate(template) {
  const body = {};
  for (const property of template.properties ?? []) {
    if (property.value !== undefined && property.value !== null) {
      body[property.name] = property.value;
      continue;
    }
    body[property.name] = defaultValueForProperty(property);
  }
  return body;
}

function defaultValueForProperty(property) {
  const name = property.name ?? '';
  if (/price|quantity|inventory|count|amount|number/i.test(name)) {
    return 1;
  }
  return `${name || 'field'}-from-template`;
}

function toUrl(apiOrHref) {
  return new URL(apiOrHref, origin).toString();
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
