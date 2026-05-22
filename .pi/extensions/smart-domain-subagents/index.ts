import { spawn } from "node:child_process";
import * as fs from "node:fs";
import * as os from "node:os";
import * as path from "node:path";
import { StringEnum } from "@earendil-works/pi-ai";
import type { ExtensionAPI } from "@earendil-works/pi-coding-agent";
import { getAgentDir, withFileMutationQueue } from "@earendil-works/pi-coding-agent";
import { Text } from "@earendil-works/pi-tui";
import { Type } from "typebox";

type AgentScope = "user" | "project" | "both";

type AgentConfig = {
  name: string;
  description: string;
  tools?: string[];
  model?: string;
  systemPrompt: string;
  source: "user" | "project";
  filePath: string;
};

type AgentResult = {
  agent: string;
  source: "user" | "project" | "unknown";
  task: string;
  exitCode: number;
  output: string;
  stderr: string;
  stopReason?: string;
  model?: string;
  step?: number;
};

type SubagentDetails = {
  mode: "single" | "parallel" | "chain";
  agentScope: AgentScope;
  agentsDir: string | null;
  results: AgentResult[];
};

const MAX_PARALLEL_TASKS = 6;
const MAX_CONCURRENCY = 3;
const OUTPUT_CAP = 50 * 1024;

function parseAgentFile(filePath: string, source: "user" | "project"): AgentConfig | null {
  const content = fs.readFileSync(filePath, "utf8");
  const match = content.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  if (!match) return null;

  const meta: Record<string, string> = {};
  for (const line of match[1].split(/\r?\n/)) {
    const colon = line.indexOf(":");
    if (colon <= 0) continue;
    meta[line.slice(0, colon).trim()] = line.slice(colon + 1).trim().replace(/^['"]|['"]$/g, "");
  }

  if (!meta.name || !meta.description) return null;
  const tools = meta.tools?.split(",").map((tool) => tool.trim()).filter(Boolean);
  return {
    name: meta.name,
    description: meta.description,
    tools: tools && tools.length > 0 ? tools : undefined,
    model: meta.model,
    systemPrompt: match[2].trim(),
    source,
    filePath,
  };
}

function loadAgentsFromDir(dir: string, source: "user" | "project"): AgentConfig[] {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true })
    .filter((entry) => (entry.isFile() || entry.isSymbolicLink()) && entry.name.endsWith(".md"))
    .map((entry) => path.join(dir, entry.name))
    .flatMap((filePath) => {
      try {
        const agent = parseAgentFile(filePath, source);
        return agent ? [agent] : [];
      } catch {
        return [];
      }
    });
}

function findProjectAgentsDir(cwd: string): string | null {
  let current = cwd;
  while (true) {
    const candidate = path.join(current, ".pi", "agents");
    if (fs.existsSync(candidate) && fs.statSync(candidate).isDirectory()) return candidate;
    const parent = path.dirname(current);
    if (parent === current) return null;
    current = parent;
  }
}

function discoverAgents(cwd: string, scope: AgentScope): { agents: AgentConfig[]; agentsDir: string | null } {
  const userAgents = scope === "project" ? [] : loadAgentsFromDir(path.join(getAgentDir(), "agents"), "user");
  const projectDir = findProjectAgentsDir(cwd);
  const projectAgents = scope === "user" || !projectDir ? [] : loadAgentsFromDir(projectDir, "project");
  const map = new Map<string, AgentConfig>();

  if (scope === "user") userAgents.forEach((agent) => map.set(agent.name, agent));
  else if (scope === "project") projectAgents.forEach((agent) => map.set(agent.name, agent));
  else {
    userAgents.forEach((agent) => map.set(agent.name, agent));
    projectAgents.forEach((agent) => map.set(agent.name, agent));
  }

  return { agents: [...map.values()], agentsDir: projectDir };
}

function getPiInvocation(args: string[]): { command: string; args: string[] } {
  const currentScript = process.argv[1];
  const isBunVirtualScript = currentScript?.startsWith("/$bunfs/root/");
  if (currentScript && !isBunVirtualScript && fs.existsSync(currentScript)) {
    return { command: process.execPath, args: [currentScript, ...args] };
  }
  const execName = path.basename(process.execPath).toLowerCase();
  if (!/^(node|bun)(\.exe)?$/.test(execName)) return { command: process.execPath, args };
  return { command: "pi", args };
}

async function writePromptToTempFile(agentName: string, prompt: string): Promise<{ dir: string; filePath: string }> {
  const dir = await fs.promises.mkdtemp(path.join(os.tmpdir(), "smart-domain-subagent-"));
  const filePath = path.join(dir, `${agentName.replace(/[^\w.-]+/g, "_")}.md`);
  await withFileMutationQueue(filePath, async () => fs.promises.writeFile(filePath, prompt, { encoding: "utf8", mode: 0o600 }));
  return { dir, filePath };
}

function extractText(message: any): string {
  if (!message || message.role !== "assistant" || !Array.isArray(message.content)) return "";
  return message.content.filter((part: any) => part?.type === "text").map((part: any) => part.text ?? "").join("\n");
}

function truncate(text: string): string {
  if (Buffer.byteLength(text, "utf8") <= OUTPUT_CAP) return text;
  let next = text.slice(0, OUTPUT_CAP);
  while (Buffer.byteLength(next, "utf8") > OUTPUT_CAP) next = next.slice(0, -1);
  return `${next}\n\n[Subagent output truncated to ${OUTPUT_CAP} bytes.]`;
}

async function runAgent(
  defaultCwd: string,
  agents: AgentConfig[],
  agentName: string,
  task: string,
  cwd: string | undefined,
  step: number | undefined,
  signal: AbortSignal | undefined,
): Promise<AgentResult> {
  const agent = agents.find((candidate) => candidate.name === agentName);
  if (!agent) {
    return {
      agent: agentName,
      source: "unknown",
      task,
      exitCode: 1,
      output: "",
      stderr: `Unknown smart-domain agent: ${agentName}. Available: ${agents.map((item) => item.name).join(", ") || "none"}`,
      step,
    };
  }

  const args = ["--mode", "json", "-p", "--no-session"];
  if (agent.model) args.push("--model", agent.model);
  if (agent.tools?.length) args.push("--tools", agent.tools.join(","));

  let tmpDir: string | null = null;
  let tmpPrompt: string | null = null;
  let output = "";
  let stderr = "";
  let stopReason: string | undefined;
  let model = agent.model;

  try {
    if (agent.systemPrompt) {
      const tmp = await writePromptToTempFile(agent.name, agent.systemPrompt);
      tmpDir = tmp.dir;
      tmpPrompt = tmp.filePath;
      args.push("--append-system-prompt", tmpPrompt);
    }
    args.push(`Task: ${task}`);

    const exitCode = await new Promise<number>((resolve) => {
      const invocation = getPiInvocation(args);
      const child = spawn(invocation.command, invocation.args, {
        cwd: cwd ?? defaultCwd,
        shell: false,
        stdio: ["ignore", "pipe", "pipe"],
      });
      let buffer = "";
      let aborted = false;

      const consumeLine = (line: string) => {
        if (!line.trim()) return;
        try {
          const event = JSON.parse(line);
          if (event.type === "message_end" && event.message) {
            const text = extractText(event.message);
            if (text) output = text;
            stopReason = event.message.stopReason ?? stopReason;
            model = event.message.model ?? model;
            if (event.message.errorMessage) stderr += `${event.message.errorMessage}\n`;
          }
        } catch {
          // Ignore non-JSON output.
        }
      };

      child.stdout.on("data", (data) => {
        buffer += data.toString();
        const lines = buffer.split("\n");
        buffer = lines.pop() ?? "";
        lines.forEach(consumeLine);
      });
      child.stderr.on("data", (data) => { stderr += data.toString(); });
      child.on("close", (code) => {
        if (buffer.trim()) consumeLine(buffer);
        resolve(aborted ? 130 : code ?? 0);
      });
      child.on("error", (error) => {
        stderr += error.message;
        resolve(1);
      });
      if (signal) {
        const abort = () => {
          aborted = true;
          child.kill("SIGTERM");
          setTimeout(() => { if (!child.killed) child.kill("SIGKILL"); }, 3000);
        };
        if (signal.aborted) abort();
        else signal.addEventListener("abort", abort, { once: true });
      }
    });

    return { agent: agent.name, source: agent.source, task, exitCode, output: truncate(output || stderr || "(no output)"), stderr, stopReason, model, step };
  } finally {
    if (tmpPrompt) fs.rmSync(tmpPrompt, { force: true });
    if (tmpDir) fs.rmSync(tmpDir, { force: true, recursive: true });
  }
}

async function mapWithLimit<T, R>(items: T[], limit: number, fn: (item: T, index: number) => Promise<R>): Promise<R[]> {
  const results = new Array<R>(items.length);
  let next = 0;
  await Promise.all(new Array(Math.min(limit, items.length)).fill(null).map(async () => {
    while (next < items.length) {
      const index = next++;
      results[index] = await fn(items[index], index);
    }
  }));
  return results;
}

const TaskItem = Type.Object({
  agent: Type.String({ description: "Agent name, for example smart-domain-domain, smart-domain-persistence, smart-domain-api, smart-domain-agent-tree, smart-domain-test" }),
  task: Type.String({ description: "Delegated task" }),
  cwd: Type.Optional(Type.String({ description: "Optional working directory" })),
});

const Params = Type.Object({
  agent: Type.Optional(Type.String({ description: "Single agent name" })),
  task: Type.Optional(Type.String({ description: "Single delegated task" })),
  tasks: Type.Optional(Type.Array(TaskItem, { description: "Parallel tasks" })),
  chain: Type.Optional(Type.Array(TaskItem, { description: "Sequential tasks; each task may use {previous}" })),
  agentScope: Type.Optional(StringEnum(["project", "user", "both"] as const, { default: "project" })),
  cwd: Type.Optional(Type.String({ description: "Optional working directory for single mode" })),
});

export default function (pi: ExtensionAPI) {
  pi.registerCommand("smart-agents", {
    description: "List Smart Domain project subagents",
    handler: async (_args, ctx) => {
      const { agents, agentsDir } = discoverAgents(ctx.cwd, "project");
      ctx.ui.notify(
        `Smart Domain agents from ${agentsDir ?? "(none)"}:\n${agents.map((agent) => `- ${agent.name}: ${agent.description}`).join("\n") || "none"}`,
        "info",
      );
    },
  });

  pi.registerTool({
    name: "smart_domain_subagent",
    label: "Smart Domain Subagent",
    description: [
      "Delegate Smart Domain work to specialized pi subagents with isolated context.",
      "Use for this repository when work needs scenario-first planning across HTTP interface, Application Logic, Persistent, agent-tree, or test boundaries.",
      "Modes: single {agent, task}, parallel {tasks}, or chain {chain with {previous}}.",
      "Project agents live in .pi/agents and default agentScope is project.",
    ].join(" "),
    promptSnippet: "Delegate Smart Domain tasks to project subagents: smart-domain-architect, smart-domain-domain, smart-domain-persistence, smart-domain-api, smart-domain-agent-tree, smart-domain-test.",
    promptGuidelines: [
      "Use smart_domain_subagent with smart-domain-architect first to produce acceptance scenarios, concrete test data, and a layer impact matrix before editing.",
      "Call only affected specialist agents for HTTP interface, Application Logic/domain, Persistent, agent-tree, or test analysis.",
      "Use smart_domain_subagent in chain mode when contracts are unclear; Application Logic/domain analysis should precede persistence or API design.",
      "Do not blindly apply subagent output; inspect the result and make final edits in the parent agent unless a workflow explicitly asks otherwise.",
    ],
    parameters: Params,
    async execute(_toolCallId, params, signal, onUpdate, ctx) {
      const agentScope = (params.agentScope ?? "project") as AgentScope;
      const { agents, agentsDir } = discoverAgents(ctx.cwd, agentScope);
      const makeDetails = (mode: SubagentDetails["mode"], results: AgentResult[]): SubagentDetails => ({ mode, agentScope, agentsDir, results });

      const single = Boolean(params.agent && params.task);
      const parallel = Boolean(params.tasks?.length);
      const chain = Boolean(params.chain?.length);
      if (Number(single) + Number(parallel) + Number(chain) !== 1) {
        return {
          content: [{ type: "text", text: `Provide exactly one mode. Available agents: ${agents.map((agent) => agent.name).join(", ") || "none"}` }],
          details: makeDetails("single", []),
        };
      }

      if (single) {
        onUpdate?.({ content: [{ type: "text", text: `Running ${params.agent}...` }], details: makeDetails("single", []) });
        const result = await runAgent(ctx.cwd, agents, params.agent!, params.task!, params.cwd, undefined, signal);
        return { content: [{ type: "text", text: result.output }], details: makeDetails("single", [result]), isError: result.exitCode !== 0 };
      }

      if (parallel) {
        if (params.tasks!.length > MAX_PARALLEL_TASKS) {
          return { content: [{ type: "text", text: `Too many parallel tasks. Max is ${MAX_PARALLEL_TASKS}.` }], details: makeDetails("parallel", []) };
        }
        const completed: AgentResult[] = [];
        const results = await mapWithLimit(params.tasks!, MAX_CONCURRENCY, async (item, index) => {
          const result = await runAgent(ctx.cwd, agents, item.agent, item.task, item.cwd, undefined, signal);
          completed.push(result);
          onUpdate?.({ content: [{ type: "text", text: `Parallel progress: ${completed.length}/${params.tasks!.length}` }], details: makeDetails("parallel", completed) });
          return result;
        });
        return {
          content: [{ type: "text", text: results.map((result, index) => `## ${index + 1}. ${result.agent}\n\n${result.output}`).join("\n\n---\n\n") }],
          details: makeDetails("parallel", results),
          isError: results.some((result) => result.exitCode !== 0),
        };
      }

      const results: AgentResult[] = [];
      let previous = "";
      for (let i = 0; i < params.chain!.length; i++) {
        const item = params.chain![i];
        const task = item.task.replace(/\{previous\}/g, previous);
        onUpdate?.({ content: [{ type: "text", text: `Chain step ${i + 1}/${params.chain!.length}: ${item.agent}` }], details: makeDetails("chain", results) });
        const result = await runAgent(ctx.cwd, agents, item.agent, task, item.cwd, i + 1, signal);
        results.push(result);
        previous = result.output;
        if (result.exitCode !== 0) {
          return { content: [{ type: "text", text: `Chain stopped at step ${i + 1} (${result.agent}).\n\n${result.output}` }], details: makeDetails("chain", results), isError: true };
        }
      }
      return { content: [{ type: "text", text: previous || "(no output)" }], details: makeDetails("chain", results) };
    },
    renderCall(args, theme) {
      const mode = args.chain?.length ? `chain:${args.chain.length}` : args.tasks?.length ? `parallel:${args.tasks.length}` : args.agent ?? "single";
      return new Text(`${theme.fg("toolTitle", theme.bold("smart_domain_subagent"))} ${theme.fg("accent", mode)}`, 0, 0);
    },
    renderResult(result, _options, theme) {
      const details = result.details as SubagentDetails | undefined;
      if (!details?.results.length) {
        const first = result.content[0];
        return new Text(first?.type === "text" ? first.text : "(no output)", 0, 0);
      }
      const lines = details.results.map((item) => {
        const icon = item.exitCode === 0 ? theme.fg("success", "✓") : theme.fg("error", "✗");
        const preview = item.output.split("\n").slice(0, 6).join("\n");
        return `${icon} ${theme.fg("accent", item.agent)} ${theme.fg("muted", `[${item.source}]`)}\n${theme.fg("toolOutput", preview)}`;
      });
      return new Text(lines.join("\n\n"), 0, 0);
    },
  });
}
