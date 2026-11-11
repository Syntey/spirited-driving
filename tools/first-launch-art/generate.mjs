// Generates the First launch illustration: one panorama five screens wide, panned a full screen per step,
// with two cars driving along the road. See README.md.
//
// Usage: node generate.mjs [--table]
//   --table  prints the road's arc length table, for picking the car positions in CARS.lead
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'build');

// One screen is 390 x 844; the panorama is five of them side by side.
const W = 390;
const H = 844;
const PAGES = 5;
const WORLD = W * PAGES;
const r1 = (v) => Math.round(v * 10) / 10;

// ---------- road ----------
// Nodes [x, y, heading in degrees (0 = east, 90 = south), handle in, handle out]. Neighbouring nodes become one cubic
// segment, so the road stays smooth through every node.
const NODES = [
  [-70, 392, -6, 0, 60],
  [95, 340, -38, 55, 55],
  [205, 222, -18, 50, 50],
  [300, 238, 42, 42, 42],
  [372, 344, 58, 42, 42],
  [470, 404, 4, 48, 48],
  [575, 372, -42, 46, 46],
  [640, 262, -68, 44, 40],
  [720, 212, -4, 40, 50],
  [880, 198, -2, 60, 60],
  [1030, 208, 2, 60, 22],
  [1072, 250, 90, 23, 23], // first hairpin
  [1030, 292, 180, 23, 60],
  [880, 292, 180, 60, 23],
  [838, 334, 90, 23, 23], // second hairpin
  [880, 376, 0, 23, 60],
  [1060, 372, -12, 70, 50],
  [1190, 292, -42, 50, 50],
  [1310, 222, -8, 50, 50],
  [1420, 262, 40, 50, 46],
  [1488, 362, 52, 42, 46],
  [1590, 404, -2, 50, 50],
  [1700, 338, -46, 48, 48],
  [1810, 262, -4, 52, 52],
  [1910, 304, 34, 46, 46],
  [2030, 336, 0, 50, 0],
];

// Cars: the MX-5 leads at these arc lengths along the road, one per step; the GR86 follows `gap` behind. Both keep to
// the right lane, `lane` from the centre line. The static illustration shows `staticStep` (the hairpins).
const CARS = { lead: [360, 860, 1470, 2350, 2840], gap: 76, lane: 5.5, staticStep: 2 };

const segs = [];
for (let i = 0; i < NODES.length - 1; i++) {
  const [x0, y0, a0, , ho] = NODES[i];
  const [x1, y1, a1, hi] = NODES[i + 1];
  const d0 = [Math.cos(a0 * Math.PI / 180), Math.sin(a0 * Math.PI / 180)];
  const d1 = [Math.cos(a1 * Math.PI / 180), Math.sin(a1 * Math.PI / 180)];
  segs.push([[x0, y0], [x0 + ho * d0[0], y0 + ho * d0[1]], [x1 - hi * d1[0], y1 - hi * d1[1]], [x1, y1]]);
}
const pt = (p) => p.map(r1).join(' ');
const roadD = 'M' + pt(segs[0][0]) + segs.map((s) => ' C' + [s[1], s[2], s[3]].map(pt).join(' ')).join('');

const bez = (s, t) => {
  const u = 1 - t;
  const p = [0, 1].map((k) => u * u * u * s[0][k] + 3 * u * u * t * s[1][k] + 3 * u * t * t * s[2][k] + t * t * t * s[3][k]);
  const d = [0, 1].map((k) => 3 * u * u * (s[1][k] - s[0][k]) + 6 * u * t * (s[2][k] - s[1][k]) + 3 * t * t * (s[3][k] - s[2][k]));
  return { x: p[0], y: p[1], a: Math.atan2(d[1], d[0]) };
};
const samples = [];
let acc = 0;
segs.forEach((s) => {
  for (let k = 0; k <= 400; k++) {
    const q = bez(s, k / 400);
    if (samples.length) {
      const last = samples[samples.length - 1];
      acc += Math.hypot(q.x - last.x, q.y - last.y);
    }
    samples.push({ ...q, s: acc });
  }
});
const roadLen = acc;
const at = (s) => {
  let lo = 0;
  let hi = samples.length - 1;
  while (hi - lo > 1) {
    const m = (lo + hi) >> 1;
    if (samples[m].s < s) lo = m; else hi = m;
  }
  return samples[hi];
};
const distRoad = (x, y) => {
  let m = Infinity;
  for (let i = 0; i < samples.length; i += 3) m = Math.min(m, Math.hypot(samples[i].x - x, samples[i].y - y));
  return m;
};

if (process.argv.includes('--table')) {
  console.log('arc length: x y heading, x relative to its page in brackets');
  for (let s = 0; s < roadLen; s += 40) {
    const p = at(s);
    const page = Math.max(0, Math.min(PAGES - 1, Math.floor(p.x / W)));
    console.log(`${s}: ${r1(p.x)} ${r1(p.y)} ${Math.round(p.a * 180 / Math.PI)}°  [page ${page + 1}, x ${r1(p.x - page * W)}]`);
  }
  process.exit(0);
}

const place = (s) => {
  const p = at(s);
  const x = p.x - Math.sin(p.a) * CARS.lane;
  const y = p.y + Math.cos(p.a) * CARS.lane;
  const deg = r1(p.a * 180 / Math.PI);
  return {
    t: `translate(${r1(x)} ${r1(y)}) rotate(${deg}) scale(1)`,
    st: `translate(${r1(x + 1.6)} ${r1(y + 2.6)}) rotate(${deg}) scale(1)`, // shadow, light from the top left
    x,
    y,
  };
};
const steps = CARS.lead.map((s) => ({ mx: place(s), gt: place(s - CARS.gap) }));

// ---------- scenery, seeded so every run draws the same picture ----------
let seed = 7;
const rnd = () => {
  seed |= 0;
  seed = seed + 0x6D2B79F5 | 0;
  let t = Math.imul(seed ^ seed >>> 15, 1 | seed);
  t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
  return ((t ^ t >>> 14) >>> 0) / 4294967296;
};

// hills: [cx, cy, rx, ry, contour rings]; the big one carries the hairpins
const HILLS = [
  [150, 150, 210, 95, 2],
  [520, 120, 170, 80, 2],
  [955, 300, 240, 170, 4],
  [1430, 120, 200, 85, 2],
  [1800, 150, 230, 100, 3],
  [330, 690, 300, 120, 2],
  [1120, 720, 280, 110, 2],
  [1750, 700, 260, 110, 2],
];
// fields: [x, y, w, h, rotation, tone]
const FIELDS = [
  [420, 300, 120, 62, -10, 1], [470, 440, 140, 70, 6, 2], [610, 420, 130, 60, -4, 1],
  [250, 300, 70, 46, 14, 2], [20, 440, 130, 60, -6, 1], [180, 470, 120, 60, 4, 2],
  [1500, 430, 140, 70, -6, 1], [1620, 220, 110, 60, -12, 2], [1640, 440, 150, 70, 4, 2],
  [1830, 380, 120, 64, 8, 1], [1210, 400, 130, 64, -8, 2], [1290, 520, 160, 70, 4, 1],
  [700, 560, 160, 70, 3, 2], [20, 600, 150, 70, -4, 2], [1880, 520, 130, 70, -6, 1],
];
const inField = (x, y, pad) => FIELDS.some(([fx, fy, fw, fh]) => x > fx - pad && x < fx + fw + pad && y > fy - pad && y < fy + fh + pad);

// river: meanders up through page 4 and passes under the road
const riverD = 'M1250 900 C1290 800 1360 760 1340 660 C1320 560 1300 500 1330 420 C1360 340 1350 290 1365 230'
  + ' C1380 170 1420 120 1450 60 C1470 20 1470 0 1480 -40';
const riverPts = [];
{
  let c0 = riverD.split(' C')[0].replace('M', '').trim().split(/\s+/).map(Number);
  riverD.split(' C').slice(1).forEach((c) => {
    const v = c.trim().split(/\s+/).map(Number);
    const sg = [c0, [v[0], v[1]], [v[2], v[3]], [v[4], v[5]]];
    for (let k = 0; k <= 200; k++) riverPts.push(bez(sg, k / 200));
    c0 = [v[4], v[5]];
  });
}
const distRiver = (x, y) => riverPts.reduce((m, p) => Math.min(m, Math.hypot(p.x - x, p.y - y)), Infinity);
let bridge = null;
{
  let best = Infinity;
  for (let i = 0; i < samples.length; i += 2) {
    const p = samples[i];
    if (p.x < 1200 || p.x > 1500) continue;
    const d = distRiver(p.x, p.y);
    if (d < best) {
      best = d;
      bridge = p;
    }
  }
}

// village on page 2: houses along a lane off the road
const laneD = 'M630 300 C600 296 572 286 540 282 C510 278 482 284 452 300';
const laneSeg = [[[630, 300], [600, 296], [572, 286], [540, 282]], [[540, 282], [510, 278], [482, 284], [452, 300]]];
const distLane = (x, y) => {
  let m = Infinity;
  laneSeg.forEach((sg) => {
    for (let k = 0; k <= 40; k++) {
      const p = bez(sg, k / 40);
      m = Math.min(m, Math.hypot(p.x - x, p.y - y));
    }
  });
  return m;
};
const roofs = [];
{
  const lp = [];
  let la = 0;
  laneSeg.forEach((sg) => {
    for (let k = 0; k <= 200; k++) {
      const q = bez(sg, k / 200);
      if (lp.length) la += Math.hypot(q.x - lp[lp.length - 1].x, q.y - lp[lp.length - 1].y);
      lp.push({ ...q, s: la });
    }
  });
  let i = 0;
  [-1, 1].forEach((side) => {
    for (let d = side < 0 ? 26 : 38; d < la - 6; d += 24) {
      const p = lp.find((q) => q.s >= d);
      const off = 16 + (i % 2) * 1.5;
      const x = p.x - Math.sin(p.a) * off * side;
      const y = p.y + Math.cos(p.a) * off * side;
      i++;
      if (distRoad(x, y) < 34) continue;
      roofs.push([r1(x), r1(y), 17 + (i % 3) * 2, 11 + (i % 2), Math.round(p.a * 180 / Math.PI + (rnd() * 8 - 4)), i % 3 === 0 ? 2 : 1]);
    }
  });
}
const nearRoof = (x, y, pad) => roofs.some(([rx, ry]) => Math.hypot(rx - x, ry - y) < pad);

// forest: clumps of trees, kept off the road, river, fields and village
const clumps = [];
const tryClump = (x, y, big) => {
  const rad = big ? 26 : 18;
  if (distRoad(x, y) < 22 + rad) return;
  if (distRiver(x, y) < 10 + rad) return;
  if (inField(x, y, 4)) return;
  if (nearRoof(x, y, 14 + rad)) return;
  if (distLane(x, y) < 10 + rad) return;
  if (bridge && Math.hypot(bridge.x - x, bridge.y - y) < 60) return;
  clumps.push([x, y, big]);
};
const WOODS = [
  [60, 200, 140, 120], [700, 100, 150, 80], [960, 290, 60, 30], [1120, 140, 120, 90],
  [1560, 140, 140, 90], [1960, 200, 100, 100], [800, 470, 120, 40], [1100, 470, 120, 40],
];
WOODS.forEach(([cx, cy, rx, ry]) => {
  for (let k = 0; k < 40; k++) {
    const t = rnd() * Math.PI * 2;
    const r = Math.sqrt(rnd());
    tryClump(cx + Math.cos(t) * rx * r, cy + Math.sin(t) * ry * r, rnd() < 0.5);
  }
});
for (let k = 0; k < 170; k++) tryClump(-20 + rnd() * (WORLD + 40), 20 + rnd() * 450, rnd() < 0.3);
for (let k = 0; k < 40; k++) tryClump(-20 + rnd() * (WORLD + 40), 480 + rnd() * 360, true); // sparser behind the panel
const trees = [];
clumps.forEach((c) => {
  if (!trees.some((k) => Math.hypot(k[0] - c[0], k[1] - c[1]) < (c[2] || k[2] ? 30 : 22))) trees.push(c);
});

// ---------- scene markup, colours as {{a.name}} holes ----------
const L = [];
L.push(`<rect x="-40" y="0" width="${WORLD + 80}" height="${H}" fill="{{a.land}}"></rect>`);
HILLS.forEach(([cx, cy, rx, ry, rings]) => {
  L.push(`<ellipse cx="${cx}" cy="${cy}" rx="${rx}" ry="${ry}" fill="{{a.hill}}"></ellipse>`);
  for (let i = 1; i <= rings; i++) {
    const f = 1 - i / (rings + 1);
    L.push(`<ellipse cx="${cx}" cy="${r1(cy + ry * 0.12 * i / rings)}" rx="${r1(rx * f)}" ry="${r1(ry * f)}" fill="none"`
      + ' stroke="{{a.contour}}" stroke-width="1.5"></ellipse>');
  }
});
FIELDS.forEach(([x, y, w, h, rot, tone]) => {
  L.push(`<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="6" fill="{{a.field${tone === 2 ? '2' : ''}}}"`
    + ` transform="rotate(${rot} ${x + w / 2} ${y + h / 2})"></rect>`);
});
L.push(`<path d="${riverD}" fill="none" stroke="{{a.riverBank}}" stroke-width="22" stroke-linecap="round"></path>`);
L.push(`<path d="${riverD}" fill="none" stroke="{{a.river}}" stroke-width="16" stroke-linecap="round"></path>`);
L.push(`<path d="${laneD}" fill="none" stroke="{{a.verge}}" stroke-width="13" stroke-linecap="round"></path>`);
L.push(`<path d="${laneD}" fill="none" stroke="{{a.asphalt}}" stroke-width="9" stroke-linecap="round"></path>`);
roofs.forEach(([x, y, w, h, rot, tone]) => {
  L.push(`<g transform="translate(${x} ${y}) rotate(${rot})"><rect x="${-w / 2}" y="${-h / 2}" width="${w}" height="${h}" rx="1.5"`
    + ` fill="{{a.roof${tone === 2 ? '2' : ''}}}"></rect><path d="M${-w / 2 + 2.5} 0 H${w / 2 - 2.5}" stroke="{{a.ridge}}"`
    + ' stroke-width="1.2"></path></g>');
});
{
  // street lamps, lit at dusk only
  const lamps = [[0, 0.55], [1, 0.1], [1, 0.6]].map(([si, t]) => {
    const p = bez(laneSeg[si], t);
    return [r1(p.x - Math.sin(p.a) * 8), r1(p.y + Math.cos(p.a) * 8)];
  });
  L.push('<g opacity="{{a.beam}}" fill="{{a.lamp}}">'
    + lamps.map(([x, y]) => `<circle cx="${x}" cy="${y}" r="7" opacity="0.18"></circle><circle cx="${x}" cy="${y}" r="1.6"></circle>`).join('')
    + '</g>');
}
L.push('<g fill="none" stroke-linecap="round" stroke-linejoin="round">');
L.push('<path d="{{road}}" stroke="{{a.verge}}" stroke-width="36"></path>');
L.push('<path d="{{road}}" stroke="{{a.asphalt}}" stroke-width="28"></path>');
L.push('<path d="{{road}}" stroke="{{a.line}}" stroke-width="1.8" stroke-dasharray="9 9"></path>');
L.push('</g>');
if (bridge) {
  L.push(`<g transform="translate(${r1(bridge.x)} ${r1(bridge.y)}) rotate(${r1(bridge.a * 180 / Math.PI)})">`);
  L.push('<rect x="-20" y="-20" width="40" height="3" rx="1.5" fill="{{a.rail}}"></rect>');
  L.push('<rect x="-20" y="17" width="40" height="3" rx="1.5" fill="{{a.rail}}"></rect>');
  L.push('</g>');
}
trees.forEach(([x, y, big]) => {
  const s = big ? 1.25 : 0.85;
  const swap = rnd() < 0.5;
  const f1 = swap ? '{{a.forest}}' : '{{a.forest2}}';
  const f2 = swap ? '{{a.forest2}}' : '{{a.forest}}';
  const crowns = [[0, 0, 16, f1], [18, 6, 13, f2], [-14, 10, 12, f1], [6, -14, 11, f2], [-6, 20, 10, f1]];
  crowns.forEach(([dx, dy, r, f]) => L.push(`<circle cx="${r1(x + dx * s)}" cy="${r1(y + dy * s)}" r="${r1(r * s)}" fill="${f}"></circle>`));
});
const scene = L.join('\n');

// ---------- cars, drawn facing +x around their centre; simplified, no badges ----------
const MX_BODY = 'M20 0 C20 -4 19.5 -6.5 17.5 -7.8 C15.5 -8.9 13 -9.1 11 -9 C7 -8.8 5 -8.3 2 -8.3 C-2 -8.3 -5 -9 -10 -9.1'
  + ' C-15 -9.2 -18.5 -8.2 -19.5 -6 C-20.2 -3.5 -20.2 3.5 -19.5 6 C-18.5 8.2 -15 9.2 -10 9.1 C-5 9 -2 8.3 2 8.3 C5 8.3 7 8.8 11 9'
  + ' C13 9.1 15.5 8.9 17.5 7.8 C19.5 6.5 20 4 20 0 Z';
const GT_BODY = 'M21.5 0 C21.5 -4.6 21.1 -7.1 19.2 -8.3 C17.2 -9.1 14.6 -9 12 -8.9 C8.5 -8.7 4.5 -8.4 0.5 -8.4'
  + ' C-3.5 -8.4 -7 -9.6 -12 -9.7 C-16.6 -9.7 -19.9 -8.9 -20.9 -6.6 C-21.6 -4 -21.6 4 -20.9 6.6 C-19.9 8.9 -16.6 9.7 -12 9.7'
  + ' C-7 9.6 -3.5 8.4 0.5 8.4 C4.5 8.4 8.5 8.7 12 8.9 C14.6 9 17.2 9.1 19.2 8.3 C21.1 7.1 21.5 4.6 21.5 0 Z';

// Red roadster after the MX-5: open cockpit, two seats, roll hoops, folded top, peaked front wings.
const mx5 = (tKey, sKey) => [
  `<path transform="{{${sKey}}}" d="${MX_BODY}" fill="{{a.shadow}}"></path>`,
  `<g transform="{{${tKey}}}">`,
  '<g opacity="{{a.beam}}" fill="{{a.lamp}}"><path d="M18 -7 L64 -24 C72 -10 72 10 64 24 L18 7 Z" opacity="0.16"></path>'
    + '<path d="M18 -6 L46 -15 C51 -6 51 6 46 15 L18 6 Z" opacity="0.22"></path></g>',
  `<path d="${MX_BODY}" fill="{{a.mx}}" stroke="{{a.edge}}" stroke-width="0.5"></path>`,
  '<path d="M17.4 -5.4 C14.2 -7 10 -7.7 5.4 -7.4 M17.4 5.4 C14.2 7 10 7.7 5.4 7.4" fill="none" stroke="{{a.mxHi}}" stroke-width="1.4"'
    + ' stroke-linecap="round"></path>',
  '<path d="M19.4 -4.3 C18.9 -6 17.8 -7.1 15.9 -7.8 L15.7 -7.1 C17.3 -6.5 18.3 -5.5 18.7 -4.2 Z M19.4 4.3 C18.9 6 17.8 7.1 15.9 7.8'
    + ' L15.7 7.1 C17.3 6.5 18.3 5.5 18.7 4.2 Z" fill="{{a.head}}"></path>',
  '<ellipse cx="3.4" cy="-9.2" rx="1.4" ry="0.9" fill="{{a.mx}}"></ellipse><ellipse cx="3.4" cy="9.2" rx="1.4" ry="0.9" fill="{{a.mx}}"></ellipse>',
  '<path d="M6.2 -7.3 C7.6 -3 7.6 3 6.2 7.3 L3.1 7.1 C4.2 3 4.2 -3 3.1 -7.1 Z" fill="{{a.glass}}"></path>',
  '<rect x="-9.6" y="-7" width="12.8" height="14" rx="2.6" fill="{{a.cabin}}"></rect>',
  '<rect x="-7.6" y="-6" width="6.4" height="5" rx="1.8" fill="{{a.seat}}"></rect><rect x="-7.6" y="1" width="6.4" height="5" rx="1.8" fill="{{a.seat}}"></rect>',
  '<rect x="0.4" y="-5.6" width="1.2" height="4.4" rx="0.6" fill="{{a.wheel}}"></rect>',
  '<rect x="-12.6" y="-6.8" width="2.4" height="13.6" rx="1.1" fill="{{a.top}}"></rect>',
  '<rect x="-10.7" y="-5.9" width="1.7" height="4.6" rx="0.8" fill="{{a.hoop}}"></rect>'
    + '<rect x="-10.7" y="1.3" width="1.7" height="4.6" rx="0.8" fill="{{a.hoop}}"></rect>',
  '<ellipse cx="-18.9" cy="-5.9" rx="1.2" ry="2.2" fill="{{a.trim}}"></ellipse><ellipse cx="-18.9" cy="5.9" rx="1.2" ry="2.2" fill="{{a.trim}}"></ellipse>',
  '<ellipse cx="-19" cy="-5.9" rx="0.8" ry="1.6" fill="{{a.tail}}"></ellipse><ellipse cx="-19" cy="5.9" rx="0.8" ry="1.6" fill="{{a.tail}}"></ellipse>',
  '</g>',
];
// White coupe after the GR86: roof between windscreen and rear glass, long bonnet, wing vents, wide rear wings,
// ducktail, tail lights joined by a black strip.
const gr86 = (tKey, sKey) => [
  `<path transform="{{${sKey}}}" d="${GT_BODY}" fill="{{a.shadow}}"></path>`,
  `<g transform="{{${tKey}}}">`,
  '<g opacity="{{a.beam}}" fill="{{a.lamp}}"><path d="M19.5 -7 L66 -24 C74 -10 74 10 66 24 L19.5 7 Z" opacity="0.16"></path>'
    + '<path d="M19.5 -6 L48 -15 C53 -6 53 6 48 15 L19.5 6 Z" opacity="0.22"></path></g>',
  `<path d="${GT_BODY}" fill="{{a.gt}}" stroke="{{a.edge}}" stroke-width="0.5"></path>`,
  '<path d="M19 -3 C14 -3.6 9.5 -3.8 5.6 -3.6 M19 3 C14 3.6 9.5 3.8 5.6 3.6" fill="none" stroke="{{a.gtShade}}" stroke-width="0.8"'
    + ' stroke-linecap="round"></path>',
  '<path d="M-2.5 -8.1 C-7 -9 -12 -9.3 -17.5 -8.7 M-2.5 8.1 C-7 9 -12 9.3 -17.5 8.7" fill="none" stroke="{{a.gtShade}}" stroke-width="1"'
    + ' stroke-linecap="round"></path>',
  '<path d="M9.8 -8.2 L7.4 -7.6 M9.8 8.2 L7.4 7.6" fill="none" stroke="{{a.vent}}" stroke-width="0.9" stroke-linecap="round"></path>',
  '<path d="M19.2 -6.5 C15.4 -7.9 10.6 -8.3 5.4 -8.1 M19.2 6.5 C15.4 7.9 10.6 8.3 5.4 8.1" fill="none" stroke="{{a.gtShade}}" stroke-width="1"'
    + ' stroke-linecap="round"></path>',
  '<path d="M20.8 -4.4 C20.3 -6.3 19 -7.6 16.9 -8.2 L16.5 -7.2 C18.3 -6.7 19.4 -5.6 19.8 -4.2 Z M20.8 4.4 C20.3 6.3 19 7.6 16.9 8.2'
    + ' L16.5 7.2 C18.3 6.7 19.4 5.6 19.8 4.2 Z" fill="{{a.head}}"></path>',
  '<ellipse cx="3.4" cy="-9.4" rx="1.4" ry="0.9" fill="{{a.gt}}"></ellipse><ellipse cx="3.4" cy="9.4" rx="1.4" ry="0.9" fill="{{a.gt}}"></ellipse>',
  '<path d="M4.8 -7.3 C5.9 -3 5.9 3 4.8 7.3 L-10.8 7.4 C-12.9 6.6 -13.6 4 -13.6 0 C-13.6 -4 -12.9 -6.6 -10.8 -7.4 Z" fill="{{a.glass}}"></path>',
  '<path d="M0.8 -6.3 C1.4 -3 1.4 3 0.8 6.3 L-8.6 6.5 C-9.4 3 -9.4 -3 -8.6 -6.5 Z" fill="{{a.gtRoof}}"></path>',
  '<rect x="-8.2" y="-0.6" width="2.4" height="1.2" rx="0.6" fill="{{a.wheel}}"></rect>',
  '<path d="M-20.4 -6.9 C-21.2 -3 -21.2 3 -20.4 6.9" fill="none" stroke="{{a.gtShade}}" stroke-width="1.3" stroke-linecap="round"></path>',
  '<rect x="-21.7" y="-4.6" width="1" height="9.2" rx="0.4" fill="{{a.trim}}"></rect>',
  '<path d="M-21.7 -4.6 L-20.7 -4.6 L-20.2 -7.9 C-20.9 -7.5 -21.4 -6.6 -21.7 -5.4 Z M-21.7 4.6 L-20.7 4.6 L-20.2 7.9'
    + ' C-20.9 7.5 -21.4 6.6 -21.7 5.4 Z" fill="{{a.tail}}"></path>',
  '</g>',
];
const carsTpl = [...gr86('gt.t', 'gt.st'), ...mx5('mx.t', 'mx.st')].join('\n');

// ---------- palettes: day for the light theme, dusk for the dark theme ----------
const day = {
  land: '#DDE9C8', hill: '#D0E2B6', contour: '#BDD3A0', forest: '#86B26F', forest2: '#73A260', field: '#EDE6BE', field2: '#E3EBB8',
  river: '#A8D0EA', riverBank: '#C7DFC0', roof: '#C98E77', roof2: '#B9A89A', ridge: 'rgba(80, 40, 30, 0.35)', rail: '#9AA1A9',
  verge: '#F6F3EA', asphalt: '#5A626C', line: '#FFFFFF',
  edge: 'rgba(0, 0, 0, 0.25)', glass: '#4B5D6E', cabin: '#2B2F34', seat: '#43484F', wheel: '#15171A', top: '#24272B', hoop: '#3A3E44',
  trim: '#1E2125', mx: '#BD162C', mxHi: '#DC4A57', gt: '#F1F2F4', gtRoof: '#FFFFFF', gtShade: '#D5D9DF', vent: '#8E959E',
  head: '#EEF2F5', tail: '#C4262E', lamp: '#FFF4CC', shadow: 'rgba(0, 0, 0, 0.2)', beam: 0,
};
const dusk = {
  land: '#1B2620', hill: '#202D25', contour: '#2A3A30', forest: '#132019', forest2: '#172720', field: '#252F21', field2: '#2A3426',
  river: '#1A3043', riverBank: '#1F2E2A', roof: '#4A3A34', roof2: '#3E3A38', ridge: 'rgba(0, 0, 0, 0.35)', rail: '#6A7078',
  verge: '#4A4F55', asphalt: '#2E3238', line: '#8C9199',
  edge: 'rgba(0, 0, 0, 0.4)', glass: '#2B3946', cabin: '#121416', seat: '#26292D', wheel: '#08090A', top: '#141618', hoop: '#22252A',
  trim: '#0E0F11', mx: '#E2303F', mxHi: '#FF6670', gt: '#BCC2CB', gtRoof: '#CDD2D9', gtShade: '#A3AAB4', vent: '#5E656E',
  head: '#FFF6D6', tail: '#FF3B3B', lamp: '#FFF1C2', shadow: 'rgba(0, 0, 0, 0.45)', beam: 1,
};
const THEMES = [['day', day], ['dusk', dusk]];

const fill = (s, pal, extra) => s.replace(/\{\{([\w.]+)\}\}/g, (m, k) => {
  if (k === 'road') return roadD;
  if (k.startsWith('a.')) return String(pal[k.slice(2)]);
  if (extra && k in extra) return extra[k];
  throw new Error('Unknown hole ' + k);
});
const svg = (w, h, viewBox, body) => `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="${viewBox}">${body}</svg>\n`;
const carsAt = (st, pal) => fill(carsTpl, pal, { 'gt.t': st.gt.t, 'gt.st': st.gt.st, 'mx.t': st.mx.t, 'mx.st': st.mx.st });

fs.mkdirSync(OUT, { recursive: true });
const write = (name, text) => fs.writeFileSync(path.join(OUT, name), text);

// ---------- 1. assets: the panorama without cars, the cars on their own, and the road data ----------
for (const [name, pal] of THEMES) {
  write(`panorama-${name}.svg`, svg(WORLD, H, `0 0 ${WORLD} ${H}`, fill(scene, pal)));
  // one car facing right, centred at 0 0, with its shadow and (at dusk) its headlight beams
  const one = (draw) => fill(draw('c.t', 'c.st').join('\n'), pal, { 'c.t': 'translate(0 0)', 'c.st': 'translate(1.6 2.6)' });
  write(`mx5-${name}.svg`, svg(200, 104, '-24 -26 100 52', one(mx5)));
  write(`gr86-${name}.svg`, svg(200, 104, '-24 -26 100 52', one(gr86)));
}
write('road.json', JSON.stringify({
  page: { width: W, height: H, count: PAGES },
  road: { path: roadD, length: r1(roadLen), segments: segs.map((s) => s.map((p) => p.map(r1))) },
  cars: CARS,
}, null, 2) + '\n');

// ---------- 2. review sheets: every step's cars, page edges, the panel area; the two cars up close ----------
for (const [name, pal] of THEMES) {
  const guides = [1, 2, 3, 4].map((i) => `<line x1="${i * W}" y1="0" x2="${i * W}" y2="${H}" stroke="#f0f" stroke-width="1"></line>`).join('')
    + `<rect x="0" y="460" width="${WORLD}" height="${H - 460}" fill="rgba(255,255,255,0.55)"></rect>`;
  write(`review-${name}.svg`, svg(WORLD, H, `0 0 ${WORLD} ${H}`, fill(scene, pal) + steps.map((st) => carsAt(st, pal)).join('\n') + guides));
  const close = { 'gt.t': 'translate(35 25)', 'gt.st': 'translate(36.6 27.6)', 'mx.t': 'translate(100 25)', 'mx.st': 'translate(101.6 27.6)' };
  write(`review-cars-${name}.svg`, svg(560, 200, '0 0 140 50', `<rect width="140" height="50" fill="${pal.asphalt}"></rect>` + fill(carsTpl, pal, close)));
}

// ---------- 3. the design canvas artboard (OnboardingArt) ----------
const stepsJs = JSON.stringify(steps.map((st) => [st.mx.t, st.mx.st, st.gt.t, st.gt.st]));
const pal = (o) => '{\n      ' + Object.entries(o).map(([k, v]) => `${k}: ${typeof v === 'number' ? v : `'${v}'`}`).reduce((rows, e) => {
  const last = rows[rows.length - 1];
  if (last && (last + ', ' + e).length < 118) rows[rows.length - 1] = last + ', ' + e; else rows.push(e);
  return rows;
}, []).join(',\n      ') + '\n    }';
write('OnboardingArt.dc.html', `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>First launch illustration</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<style>
body{margin:0}
</style>
</helmet>
<div style="width: 390px; height: 844px; position: relative; overflow: hidden; background: {{a.land}}">
<svg width="390" height="844" viewBox="0 0 390 844" aria-hidden="true" style="display: block; position: absolute; inset: 0">
<g transform="translate({{tx}} 0)">
${scene}
${carsTpl}
</g>
</svg>
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{"dark":{"editor":"boolean","default":false},"step":{"editor":"int","min":0,"max":4,"default":0},"art":{"editor":"enum","options":["slide","static"],"default":"slide"},"$preview":{"width":390,"height":844}}'>
class Component extends DCLogic {
  renderVals() {
    const dark = this.props.dark ?? false;
    const art = this.props.art ?? 'slide';
    const raw = Math.max(0, Math.min(4, Math.round(Number(this.props.step ?? 0) || 0)));
    // One panorama five screens wide; each step pans a full screen, so the road runs on from page to page.
    const step = art === 'static' ? ${CARS.staticStep} : raw;
    const a = dark ? ${pal(dusk)} : ${pal(day)};
    const road = '${roadD}';
    // Per step: MX-5 (lead) transform and shadow, GR86 (follows) transform and shadow.
    const cars = ${stepsJs};
    const c = cars[step];
    return {
      a, road, tx: -${W} * step,
      mx: { t: c[0], st: c[1] },
      gt: { t: c[2], st: c[3] }
    };
  }
}
</script>
</body>
</html>
`);

// ---------- 4. the interactive prototype page ----------
{
  const tagged = carsTpl
    .replace('transform="{{mx.st}}"', 'data-shadow="mx"').replace('transform="{{mx.t}}"', 'data-car="mx"')
    .replace('transform="{{gt.st}}"', 'data-shadow="gt"').replace('transform="{{gt.t}}"', 'data-car="gt"');
  const data = {
    scene: { day: fill(scene, day), dusk: fill(scene, dusk) },
    cars: { day: fill(tagged, day), dusk: fill(tagged, dusk) },
    land: { day: day.land, dusk: dusk.land },
    segs: segs.map((s) => s.map((p) => p.map(r1))),
    leads: CARS.lead, gap: CARS.gap, lane: CARS.lane, staticStep: CARS.staticStep,
  };
  const template = fs.readFileSync(path.join(HERE, 'prototype.template.html'), 'utf8');
  write('prototype.html', template.replace('__DATA__', () => JSON.stringify(data).replace(/<\//g, '<\\/')));
}

// ---------- 5. the app: the same scene as Kotlin draw calls for Compose ----------
// Compose's vector images can't draw the road's dashed centre line, so the app draws the scene in code. This turns the
// scene's markup (the small part of SVG used above) into calls to the helpers in ArtDrawing.kt, with the palette as `p`.
{
  const PACKAGE = 'com.syntey.spiriteddriving.feature.onboarding.art';
  const KT = path.join(HERE, '../../feature/onboarding/src/commonMain/kotlin', ...PACKAGE.split('.'), 'OnboardingArtScene.kt');

  const num = (v) => {
    const s = String(v).trim();
    if (!/^-?\d+(\.\d+)?$/.test(s)) throw new Error('Not a plain number: ' + s);
    return s.replace(/^(-?)0+(\d)/, '$1$2') + 'f';
  };
  const nums = (s) => s.trim().split(/[\s,]+/).map(num);
  const colour = (v) => {
    const hole = /^\{\{a\.(\w+)\}\}$/.exec(v);
    if (hole) return 'p.' + hole[1];
    throw new Error('Colours come from the palette: ' + v);
  };
  const ktColour = (v) => {
    let m = /^#([0-9A-F]{6})$/i.exec(v);
    if (m) return `Color(0xFF${m[1].toUpperCase()})`;
    m = /^rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)$/.exec(v);
    if (m) {
      const hex = (n) => Number(n).toString(16).padStart(2, '0').toUpperCase();
      return `Color(0x${hex(Math.round(Number(m[4]) * 255))}${hex(m[1])}${hex(m[2])}${hex(m[3])})`;
    }
    throw new Error('Unknown colour ' + v);
  };

  // Paths are parsed once, so each distinct one becomes a property of ArtPaths.
  const paths = new Map();
  const pathRef = (d) => {
    if (!paths.has(d)) paths.set(d, 'path' + paths.size);
    return 'ArtPaths.' + paths.get(d);
  };

  // Turns markup into Kotlin statements. Groups pass fill and line caps down to their children; a group whose opacity is
  // `beam` (lights, only at dusk) becomes an `if`, and a group transform a `placed` block.
  const toKotlin = (markup) => {
    const out = [];
    const stack = [{ fill: null, cap: null }];
    let indent = 1;
    const emit = (line) => out.push('    '.repeat(indent) + line);
    for (const [, close, tag, attrText] of markup.matchAll(/<(\/?)(\w+)([^>]*)>/g)) {
      if (close) {
        if (tag !== 'g') continue;
        const group = stack.pop();
        for (let i = 0; i < group.blocks; i++) {
          indent--;
          emit('}');
        }
        continue;
      }
      const a = Object.fromEntries([...attrText.matchAll(/([\w-]+)="([^"]*)"/g)].map((m) => [m[1], m[2]]));
      const parent = stack[stack.length - 1];
      const fill = a.fill ?? parent.fill;
      const cap = a['stroke-linecap'] ?? parent.cap;
      if (tag === 'g') {
        let blocks = 0;
        if (a.opacity === '{{a.beam}}') {
          emit('if (p.lightsOn) {');
          indent++;
          blocks++;
        }
        const t = a.transform && /^translate\(([^)]*)\) rotate\(([^)]*)\)$/.exec(a.transform);
        if (t) {
          const [x, y] = nums(t[1]);
          emit(`placed(${x}, ${y}, ${nums(t[2])[0]}) {`);
          indent++;
          blocks++;
        } else if (a.transform && !a.transform.startsWith('{{')) {
          throw new Error('Unsupported group transform ' + a.transform);
        }
        stack.push({ fill, cap, blocks });
        continue;
      }

      const alpha = a.opacity ? `, alpha = ${num(a.opacity)}` : '';
      const shapes = [];
      const fillColour = fill && fill !== 'none' ? colour(fill) : null;
      if (tag === 'circle') {
        shapes.push(`circle(${num(a.cx)}, ${num(a.cy)}, ${num(a.r)}, ${fillColour}${alpha})`);
      } else if (tag === 'ellipse') {
        const geometry = `${num(a.cx)}, ${num(a.cy)}, ${num(a.rx)}, ${num(a.ry)}`;
        if (fillColour) shapes.push(`ellipse(${geometry}, ${fillColour})`);
        if (a.stroke) shapes.push(`ellipseOutline(${geometry}, ${colour(a.stroke)}, ${num(a['stroke-width'])})`);
      } else if (tag === 'rect') {
        shapes.push(`roundRect(${num(a.x)}, ${num(a.y)}, ${num(a.width)}, ${num(a.height)}, ${a.rx ? num(a.rx) : '0f'}, ${fillColour}${alpha})`);
      } else if (tag === 'path') {
        const ref = pathRef(a.d);
        if (fillColour) shapes.push(`fillPath(${ref}, ${fillColour}${alpha})`);
        if (a.stroke) {
          const dash = a['stroke-dasharray'] ? `, dash = floatArrayOf(${nums(a['stroke-dasharray']).join(', ')})` : '';
          const round = cap === 'round' ? ', round = true' : '';
          shapes.push(`strokePath(${ref}, ${colour(a.stroke)}, ${num(a['stroke-width'])}${round}${dash}${alpha})`);
        }
      } else {
        throw new Error('Unsupported element ' + tag);
      }
      if (!shapes.length || shapes.some((s) => s.includes(', null'))) throw new Error(`<${tag}> needs a fill or a stroke`);

      const rot = a.transform && /^rotate\(([^)]*)\)$/.exec(a.transform);
      if (rot) {
        const [deg, px, py] = nums(rot[1]);
        emit(`rotated(${deg}, ${px}, ${py}) { ${shapes.join('; ')} }`);
      } else if (a.transform && !a.transform.startsWith('{{')) {
        throw new Error('Unsupported transform ' + a.transform);
      } else {
        shapes.forEach(emit);
      }
    }
    return out;
  };

  // The scenery in parts of a few hundred calls, so no compiled function gets too big. Parts end between top-level shapes.
  const sceneryParts = [];
  {
    let part = [];
    let depth = 0;
    for (const line of scene.split('\n')) {
      part.push(line);
      depth += (line.match(/<g[\s>]/g) || []).length - (line.match(/<\/g>/g) || []).length;
      if (depth === 0 && part.length >= 250) {
        sceneryParts.push(part.join('\n'));
        part = [];
      }
    }
    if (part.length) sceneryParts.push(part.join('\n'));
  }
  const withRoad = (s) => s.replace(/\{\{road\}\}/g, roadD);
  const fn = (name, body, visibility = 'private') =>
    [`${visibility} fun DrawScope.${name}(p: ArtPalette) {`, ...body, '}'].join('\n');

  // A car: its shadow, drawn at the car's place plus the shadow offset, and the car itself, facing +x around 0,0.
  const car = (name, draw) => {
    const lines = draw('c.t', 'c.st');
    return [
      fn(`draw${name}Shadow`, toKotlin(lines[0]), 'internal'),
      fn(`draw${name}`, toKotlin(lines.slice(2, -1).join('\n')), 'internal'),
    ].join('\n\n');
  };

  const functions = [
    fn('drawScenery', sceneryParts.map((_, i) => `    scenery${i}(p)`), 'internal'),
    ...sceneryParts.map((s, i) => fn(`scenery${i}`, toKotlin(withRoad(s)))),
    car('Mx5', mx5),
    car('Gr86', gr86),
  ];

  const keys = Object.keys(day).filter((k) => k !== 'beam');
  const palette = (name, pal) => [
    `        val ${name} = ArtPalette(`,
    ...keys.map((k) => `            ${k} = ${ktColour(pal[k])},`),
    `            lightsOn = ${pal.beam > 0},`,
    '        )',
  ].join('\n');

  const floats = (values, perLine) => {
    const rows = [];
    for (let i = 0; i < values.length; i += perLine) rows.push('        ' + values.slice(i, i + perLine).map(num).join(', ') + ',');
    return rows.join('\n');
  };
  // Long paths are split into lines of at most 100 characters, at a command.
  const splitPath = (d) => {
    const parts = [];
    let rest = d;
    while (rest.length > 100) {
      let cut = rest.lastIndexOf(' C', 100);
      if (cut <= 0) cut = rest.lastIndexOf(' ', 100);
      parts.push(rest.slice(0, cut + 1));
      rest = rest.slice(cut + 1);
    }
    parts.push(rest);
    return parts;
  };
  const pathProps = [...paths.entries()].map(([d, name]) => {
    const parts = splitPath(d);
    if (parts.length === 1) return `    val ${name} = svgPath("${d}")`;
    const lines = parts.map((s, i) => `${i === 0 ? '        ' : '            '}"${s}"${i < parts.length - 1 ? ' +' : ','}`);
    return [`    val ${name} = svgPath(`, ...lines, '    )'].join('\n');
  });

  const kotlin = `// Generated by tools/first-launch-art/generate.mjs from the same scene as the design canvas and the prototype.
// Don't edit it: change the generator and run it again.
@file:Suppress("MagicNumber", "LongMethod", "TooManyFunctions", "LargeClass", "LongParameterList")

package ${PACKAGE}

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * The illustration's colours: [Day] goes with the light theme, [Dusk], with the lights on, with the dark theme.
 */
internal class ArtPalette(
${keys.map((k) => `    val ${k}: Color,`).join('\n')}
    val lightsOn: Boolean,
) {
    companion object {
${palette('Day', day)}

${palette('Dusk', dusk)}
    }
}

/**
 * The panorama's size and the road the cars drive along, in the panorama's units: one page is ${W} × ${H}.
 */
internal object ArtRoad {
    const val PAGE_WIDTH = ${num(W)}
    const val PAGE_HEIGHT = ${num(H)}
    const val PAGE_COUNT = ${PAGES}

    /** The road as cubic segments of 8 numbers: start, both control points and end, each as x and y. */
    val segments = floatArrayOf(
${floats(segs.flat(2).map(r1), 8)}
    )

    /** How far along the road the lead car is on each page. */
    val leads = floatArrayOf(${CARS.lead.map(num).join(', ')})

    /** How far the second car follows the lead car. */
    const val GAP = ${num(CARS.gap)}

    /** How far right of the centre line the cars drive. */
    const val LANE = ${num(CARS.lane)}

    /** The light comes from the top left, so shadows fall this far right and down. */
    const val SHADOW_X = 1.6f
    const val SHADOW_Y = 2.6f
}

${functions.join('\n\n')}

private object ArtPaths {
${pathProps.join('\n')}
}
`;
  fs.writeFileSync(KT, kotlin);
  console.log(`Wrote ${path.relative(path.join(HERE, '../..'), KT)}: ${paths.size} paths, ${sceneryParts.length} scenery parts`);
}

console.log(`Road ${Math.round(roadLen)} long, ${trees.length} tree clumps, bridge at ${r1(bridge.x)} ${r1(bridge.y)}. Wrote ${OUT}`);
steps.forEach((st, i) => console.log(`Page ${i + 1}: MX-5 at ${r1(st.mx.x - i * W)} ${r1(st.mx.y)}, GR86 at ${r1(st.gt.x - i * W)} ${r1(st.gt.y)}`));
