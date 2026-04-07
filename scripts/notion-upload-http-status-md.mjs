import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const mdPath = path.join(__dirname, "..", "docs", "HTTP_STATUS_CODES.md");
let md = fs.readFileSync(mdPath, "utf8");
md = md.replace(/\r\n/g, "\n");

const h1 = md.match(/^#\s+(.+)$/m);
const title = h1 ? h1[1].trim() : "HTTP 응답 상태 코드 정리";
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

body = body.replace(/(?:^|\n)(\|[^\n]+\|\n\|[-\s|]+\|\n(?:\|[^\n]+\|\n?)+)/g, (m) => {
  return "\n" + mdTableToHtml(m) + "\n";
});

const content = body.trim() + "\n<empty-block/>\n";

const payload = {
  parent: {
    type: "page_id",
    page_id: "33981af1-4757-8143-8be3-f3f6d990a3ac",
  },
  pages: [
    {
      properties: { title },
      content,
    },
  ],
};

const out = path.join(__dirname, "..", "notion-http-status-payload.json");
fs.writeFileSync(out, JSON.stringify(payload), "utf8");
console.log("OK", title, "contentLen", content.length, "->", out);
