import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const mdPath = path.join(__dirname, "..", "docs", "EXCEPTION_TYPES.md");
let md = fs.readFileSync(mdPath, "utf8");
md = md.replace(/\r\n/g, "\n");

// Page title: first H1 only; body without duplicate title (Notion spec)
const h1 = md.match(/^#\s+(.+)$/m);
const title = h1 ? h1[1].trim() : "예외 타입 설명";
let body = md.replace(/^#\s+.+\n+/, "");

function mdTableToHtml(block) {
  const lines = block.trim().split("\n").filter((l) => l.trim().startsWith("|"));
  if (lines.length < 2) return block;
  const rows = [];
  for (const line of lines) {
    if (/^\|[\s\-:|]+\|$/.test(line)) continue;
    const parts = line.split("|");
    const cells = parts
      .slice(1, -1)
      .map((c) => c.trim())
      .filter((c) => c.length);
    if (cells.length) rows.push(cells);
  }
  if (!rows.length) return block;
  let html = "<table fit-page-width=\"true\" header-row=\"true\">\n";
  rows.forEach((cells) => {
    html += "<tr>\n";
    cells.forEach((c) => {
      html += `<td>${c}</td>\n`;
    });
    html += "</tr>\n";
  });
  html += "</table>";
  return html;
}

// Replace markdown pipe tables (| a | b |) with HTML tables
body = body.replace(/(?:^|\n)(\|[^\n]+\|\n\|[-\s|]+\|\n(?:\|[^\n]+\|\n?)+)/g, (m) => {
  return "\n" + mdTableToHtml(m) + "\n";
});

// Horizontal rules: keep as ---
body = body.replace(/\n---\n/g, "\n---\n");

const content = body.trim() + "\n<empty-block/>\n";

const payload = {
  pages: [
    {
      properties: { title },
      content,
    },
  ],
};

fs.writeFileSync(
  path.join(__dirname, "..", "notion-exception-page.json"),
  JSON.stringify(payload),
  "utf8"
);
console.log("OK", title, "length", content.length);
