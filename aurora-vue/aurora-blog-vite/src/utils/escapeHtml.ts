const escapeMap: Record<string, string> = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
}

export default function escapeHtml(str: string) {
  return String(str).replace(/[&<>"']/g, (char) => escapeMap[char])
}
