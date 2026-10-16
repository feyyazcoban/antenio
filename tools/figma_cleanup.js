// Figma use_figma script. Load figma-use skill first. This file has NOT been executed.
// Target: KYAb9p9Aqu49vnXKZXXpcx, personal account. Read nodes before retrying.
const page = await figma.getNodeByIdAsync('87:3626');
await figma.setCurrentPageAsync(page);
const replacements = {
  '204:11': 'Foundations: temel sistem.\nComponents: mevcut TV kiti ve ürün bileşenleri.\n03 Product & Screens: kapsam, akışlar, ekranlar ve teslim.',
  '204:45': 'TV-030 / Movies / Default\nTV-030 / Movies / Empty\nHer ekran: Draft → Review → Ready. Durum manifestte de güncellenir.'
};
const mutatedNodeIds = [];
for (const [id, value] of Object.entries(replacements)) {
  const node = await figma.getNodeByIdAsync(id);
  if (!node || node.type !== 'TEXT') throw new Error('Expected TEXT node ' + id);
  const expected = id === '204:11' ? 'Foundations' : 'TV-030';
  if (!node.characters.includes(expected)) throw new Error('Unexpected node content; re-read canvas: ' + id);
  await Promise.all(node.getStyledTextSegments(['fontName']).map(s => figma.loadFontAsync(s.fontName)));
  node.characters = value;
  mutatedNodeIds.push(id);
}
return { mutatedNodeIds };
