(() => {
  'use strict';
  const data = window.IRISEN_SCHEMA;
  const $ = id => document.getElementById(id);
  const byName = new Map(data.tables.map(t => [t.name, t]));
  const ns = 'http://www.w3.org/2000/svg';
  let selected = byName.has(location.hash.slice(1)) ? location.hash.slice(1) : 'post';
  let currentRelations = [], visible = [], view = {x:0, y:0, w:1000, h:800}, bounds = view;
  const el = (tag, text, className) => {
    const node = document.createElement(tag);
    if (text !== undefined) node.textContent = text;
    if (className) node.className = className;
    return node;
  };
  const svgEl = (tag, attrs, text) => {
    const node = document.createElementNS(ns, tag);
    for (const [key,value] of Object.entries(attrs)) node.setAttribute(key, value);
    if (text) node.textContent = text;
    return node;
  };
  const relationsFor = name => data.relations.filter(r => r.source === name || r.target === name);
  const relationLabel = r => `${r.source}.(${r.columns.join(', ')}) → ${r.target}.(${r.targetColumns.join(', ')})`;
  const filteredRelations = () => data.relations.filter(r => !$('kind').value || r.kind === $('kind').value);
  $('stats').textContent = `${data.tables.length} 테이블 / ${data.tables.reduce((n,t) => n+t.columns.length,0)} 컬럼 / ${data.relations.length} 관계`;
  $('snapshot-date').textContent = data.date;

  function choose(name) {
    selected = name;
    history.replaceState(null, '', '#' + name);
    renderDetail(); renderList(); renderGraph();
  }
  function renderList() {
    const term = $('search').value.trim().toLowerCase();
    visible = data.tables.filter(t => (!$('group').value || t.group === $('group').value) &&
      `${t.name} ${t.description} ${t.columns.map(c=>c.name).join(' ')}`.toLowerCase().includes(term));
    $('table-count').textContent = `${visible.length} / ${data.tables.length}`;
    $('table-list').replaceChildren();
    for (const t of visible) {
      const button = el('button',t.name,`table-item${t.name === selected ? ' active':''}`);
      button.setAttribute('aria-current',String(t.name === selected));
      button.append(el('small',`${t.group} · ${t.columns.length} columns`));
      button.onclick = () => choose(t.name);
      $('table-list').append(button);
    }
  }
  function applyView() {
    $('graph').setAttribute('viewBox',`${view.x} ${view.y} ${view.w} ${view.h}`);
    $('zoom-level').textContent = Math.round(bounds.w / view.w * 100) + '%';
  }
  function fit() {
    const ratio = $('map').clientWidth / Math.max($('map').clientHeight,1);
    view = {...bounds};
    if(view.w/view.h < ratio) {const w=view.h*ratio;view.x-=(w-view.w)/2;view.w=w;}
    else {const h=view.w/ratio;view.y-=(h-view.h)/2;view.h=h;}
    applyView();
  }
  function zoom(factor, px=.5, py=.5) {
    const w = Math.min(bounds.w*5, Math.max(200, view.w * factor));
    const h = w * view.h/view.w;
    view = {x:view.x+(view.w-w)*px,y:view.y+(view.h-h)*py,w,h}; applyView();
  }
  function renderGraph() {
    let tables = visible;
    const relations = filteredRelations();
    if ($('focus').checked) {
      const neighbors = new Set([selected]);
      relations.filter(r=>r.source===selected || r.target===selected).forEach(r=>{neighbors.add(r.source);neighbors.add(r.target);});
      tables = tables.filter(t=>neighbors.has(t.name));
    }
    const names = new Set(tables.map(t=>t.name));
    const edges = relations.filter(r=>names.has(r.source)&&names.has(r.target));
    $('map-count').textContent = `${tables.length} 노드 · ${edges.length} 연결`;
    $('empty').hidden = tables.length > 0;
    const svg = $('graph'); svg.replaceChildren();
    const defs = svgEl('defs',{});
    for(const [id,color] of [['fk','#879caa'],['logical','#bc914d'],['active','#087e8b']]) {
      const marker=svgEl('marker',{id,viewBox:'0 0 10 10',refX:9,refY:5,markerWidth:7,markerHeight:7,orient:'auto-start-reverse'});
      marker.append(svgEl('path',{d:'M 0 0 L 10 5 L 0 10 z',fill:color}));defs.append(marker);
    }
    svg.append(defs);
    // Grouped columns keep the overview deterministic and unrelated tables visible.
    const positions=new Map();let maxRows=1;
    ['콘텐츠','보드','계정·운영'].forEach((group,col)=>{
      const groupTables=tables.filter(t=>t.group===group);
      if(groupTables.length) svg.append(svgEl('text',{x:col*350+45,y:30,fill:'#6c8797','font-size':13},group));
      groupTables.forEach((t,row)=>positions.set(t.name,{x:col*350+35,y:row*120+60}));
      maxRows=Math.max(maxRows,groupTables.length);
    });
    bounds={x:0,y:0,w:1070,h:maxRows*120+85};
    for(const [index,r] of edges.entries()) {
      const a=positions.get(r.source),b=positions.get(r.target),active=r.source===selected||r.target===selected;
      const color=active?'#087e8b':r.kind==='FK'?'#879caa':'#bc914d';
      let d;
      if(r.source===r.target) d=`M ${a.x+240} ${a.y+22} C ${a.x+305} ${a.y-25},${a.x+305} ${a.y+95},${a.x+240} ${a.y+55}`;
      else if(a.x===b.x) {const x=a.x+265+(index%5)*8;d=`M ${a.x+240} ${a.y+35} C ${x} ${a.y+35},${x} ${b.y+35},${b.x+240} ${b.y+35}`;}
      else {const forward=a.x<b.x,sx=a.x+(forward?240:0),tx=b.x+(forward?0:240),mid=(sx+tx)/2;d=`M ${sx} ${a.y+35} C ${mid} ${a.y+35},${mid} ${b.y+35},${tx} ${b.y+35}`;}
      const path=svgEl('path',{d,stroke:color,class:`edge${active?' active':''}`,'stroke-dasharray':r.kind==='논리'?'6 5':'none','marker-end':`url(#${active?'active':r.kind==='FK'?'fk':'logical'})`,opacity:active?1:.38});
      path.append(svgEl('title',{},`${relationLabel(r)} · ${r.kind} · ${r.note}`));svg.append(path);
    }
    for(const t of tables) {
      const {x,y}=positions.get(t.name);
      const node=svgEl('g',{transform:`translate(${x},${y})`,class:`node${t.name===selected?' selected':''}`,tabindex:0,role:'button','aria-label':`${t.name} 테이블 상세 조회`});
      node.append(svgEl('rect',{width:240,height:76,rx:9,class:'node-box'}));
      node.append(svgEl('rect',{x:0,y:14,width:3,height:46,rx:1,fill:t.group==='콘텐츠'?'#087e8b':t.group==='보드'?'#6488c8':'#c18c50'}));
      node.append(svgEl('text',{x:13,y:28,class:'node-title'},t.name));
      node.append(svgEl('text',{x:13,y:51,class:'node-sub'},`${t.columns.length} columns · ${relationsFor(t.name).length} relations`));
      node.append(svgEl('title',{},t.description));
      node.addEventListener('click',()=>{if(!dragMoved) choose(t.name);});
      node.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();choose(t.name);}});
      svg.append(node);
    }
    fit();
  }
  function renderDetail() {
    const t=byName.get(selected),detail=$('detail');detail.replaceChildren();
    detail.append(el('div','TABLE INSPECTOR','section-label'),el('h2',t.name,'table-title'),el('p',t.description,'muted'));
    const badges=el('div',undefined,'badges');
    [t.group,`${t.columns.length} 컬럼`,`${relationsFor(selected).length} 관계`].forEach(v=>badges.append(el('span',v,'badge')));detail.append(badges);
    const table=el('table',undefined,'schema-table'),head=el('thead'),tr=el('tr');
    ['컬럼 / 키','타입','NULL'].forEach(v=>tr.append(el('th',v)));head.append(tr);table.append(head);
    const body=el('tbody');
    for(const c of t.columns) {
      const row=el('tr'),name=el('td',c.name);
      if(c.key.startsWith('PK')) name.append(el('small',c.key.match(/PK\(\d+\)/)[0]));
      name.title=`기본값: ${c.default}\n${c.key}`;
      row.append(name,el('td',c.type),el('td',c.nullable));body.append(row);
    }
    table.append(body);const scroll=el('div',undefined,'table-scroll');scroll.append(table);detail.append(scroll);
    detail.append(el('h3','연결된 테이블'));
    const links=el('div',undefined,'relationships');
    currentRelations=relationsFor(selected);
    if(!currentRelations.length) links.append(el('p','직접 참조 관계가 없는 테이블입니다.','muted'));
    for(const r of currentRelations) {
      const button=el('button',relationLabel(r),'relation');button.append(el('small',`${r.kind} · ${r.note}`));
      button.onclick=()=>{ $('search').value='';$('group').value='';choose(r.source===selected?r.target:r.source);};links.append(button);
    }
    detail.append(links);
    $('join').replaceChildren(new Option('선택 테이블만 조회',''));
    currentRelations.forEach((r,i)=>$('join').add(new Option(`${r.kind} · ${relationLabel(r)}`,String(i))));
    $('column-picker').replaceChildren();
    for(const c of t.columns) {
      const label=el('label'),input=el('input');input.type='checkbox';input.value=c.name;input.checked=c.key.startsWith('PK');input.onchange=renderSql;
      label.append(input,document.createTextNode(c.name));$('column-picker').append(label);
    }
    if(!$('column-picker').querySelector(':checked')) $('column-picker').querySelector('input').checked=true;
    renderSql();
  }
  const quote = value => '`'+value.replaceAll('`','``')+'`';
  function renderSql() {
    const columns=[...$('column-picker').querySelectorAll('input:checked')].map(n=>n.value);
    $('copy-status').textContent='';$('copy').disabled=!columns.length;
    if(!columns.length){$('sql').textContent='조회할 컬럼을 하나 이상 선택하세요.';return;}
    const limit=Math.min(1000,Math.max(1,Math.trunc(Number($('limit').value)||100)));
    const r=$('join').value===''?null:currentRelations[Number($('join').value)];
    let sql=`SELECT\n  ${columns.map(c=>'t.'+quote(c)).join(',\n  ')}${r?',\n  related.*':''}\nFROM ${quote(selected)} AS t`;
    if(r){
      const outgoing=r.source===selected,target=outgoing?r.target:r.source;
      const left=outgoing?r.columns:r.targetColumns,right=outgoing?r.targetColumns:r.columns;
      sql+=`\nLEFT JOIN ${quote(target)} AS related\n  ON ${left.map((c,i)=>`t.${quote(c)} = related.${quote(right[i])}`).join('\n AND ')}`;
    }
    $('sql').textContent=sql+`\nLIMIT ${limit};`;
  }
  ['search','group'].forEach(id=>$(id).addEventListener('input',()=>{renderList();renderGraph();}));
  ['kind','focus'].forEach(id=>$(id).addEventListener('change',renderGraph));
  ['join','limit'].forEach(id=>$(id).addEventListener('input',renderSql));
  $('select-all').onclick=()=>{ $('column-picker').querySelectorAll('input').forEach(n=>n.checked=true);renderSql();};
  $('select-pk').onclick=()=>{ const cols=byName.get(selected).columns;$('column-picker').querySelectorAll('input').forEach(n=>n.checked=cols.find(c=>c.name===n.value).key.startsWith('PK'));renderSql();};
  $('copy').onclick=async()=>{
    try{await navigator.clipboard.writeText($('sql').textContent);$('copy-status').textContent='복사했습니다.';}
    catch{const range=document.createRange();range.selectNodeContents($('sql'));const selection=window.getSelection();selection.removeAllRanges();selection.addRange(range);$('copy-status').textContent='선택된 SQL을 Ctrl+C로 복사하세요.';}
  };
  $('fit').onclick=fit;$('zoom-in').onclick=()=>zoom(.8);$('zoom-out').onclick=()=>zoom(1.25);
  let drag=null,dragMoved=false;
  $('graph').addEventListener('pointerdown',e=>{if(e.button!==0)return;drag={x:e.clientX,y:e.clientY,view:{...view}};dragMoved=false;});
  window.addEventListener('pointermove',e=>{
    if(!drag)return;const dx=e.clientX-drag.x,dy=e.clientY-drag.y;
    if(Math.abs(dx)+Math.abs(dy)>4)dragMoved=true;
    view={...drag.view,x:drag.view.x-dx*drag.view.w/$('graph').clientWidth,y:drag.view.y-dy*drag.view.h/$('graph').clientHeight};applyView();
  });
  window.addEventListener('pointerup',()=>{drag=null;});window.addEventListener('pointercancel',()=>{drag=null;});
  $('graph').addEventListener('wheel',e=>{e.preventDefault();const rect=$('graph').getBoundingClientRect();zoom(e.deltaY>0?1.12:1/1.12,(e.clientX-rect.left)/rect.width,(e.clientY-rect.top)/rect.height);},{passive:false});
  window.addEventListener('hashchange',()=>{const name=location.hash.slice(1);if(byName.has(name))choose(name);});
  new ResizeObserver(fit).observe($('map'));
  renderList();renderDetail();renderGraph();
})();
