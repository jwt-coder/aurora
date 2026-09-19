import mermaidPlugin from "@agoose77/markdown-it-mermaid"
import MarkdownIt from 'markdown-it'
import mkKatexExternal from 'markdown-it-katex-external'
import mkEmoji from 'markdown-it-emoji'
import mkContainer from 'markdown-it-container'
import mkSup from 'markdown-it-sup'
import mkSub from 'markdown-it-sub'
import mkFootnote from 'markdown-it-footnote'
import mkAbbr from 'markdown-it-abbr'
import mkIns from 'markdown-it-ins'
import mkMark from 'markdown-it-mark'
import mkKatex from '@iktakahiro/markdown-it-katex'

// 单例：每次 new MarkdownIt + 挂全套插件开销很大，列表/详情会反复调用
let md: MarkdownIt | null = null

function getMd(): MarkdownIt {
  if (!md) {
    md = new MarkdownIt({ html: true })
      .use(mkKatexExternal)
      .use(mkEmoji)
      .use(mkContainer, 'hljs-center')
      .use(mkContainer, 'hljs-left')
      .use(mkContainer, 'hljs-right')
      .use(mkSup)
      .use(mkSub)
      .use(mkFootnote)
      .use(mkAbbr)
      .use(mkIns)
      .use(mkMark)
      .use(mkKatex)
      .use(mermaidPlugin)
  }
  return md
}

export default function markdownToHtml(content: any) {
  return getMd().render(content || '')
}

/** 摘要用：去掉标签即可，不必走完整 markdown 管线 */
export function stripHtml(content: any, maxLen?: number) {
  let text = String(content || '')
    .replace(/<\/?[^>]*>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&[a-zA-Z#0-9]+;/g, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
  if (maxLen && maxLen > 0 && text.length > maxLen) {
    text = text.slice(0, maxLen) + '…'
  }
  return text
}
