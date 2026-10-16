/* Visual revision only. Uses the previously reviewed, sanitized local catalog. */
(() => {
  'use strict';
  const $ = s => document.querySelector(s), app = $('#app'), tv = $('#tv'), overlay = $('#overlay');
  const esc = x => String(x ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const paths = {
    search:'M21 21l-5-5M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0',
    settings:'M9 3h6l1 3 3 1 2 5-2 5-3 1-1 3H9l-1-3-3-1-2-5 2-5 3-1ZM12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8',
    play:'m8 4 12 8-12 8Z',plus:'M12 4v16M4 12h16',check:'m4 12 5 5L20 6',back:'m14 5-7 7 7 7M7 12h14',
    right:'m9 5 7 7-7 7',film:'M3 3h18v18H3ZM7 3v18M17 3v18M3 8h4M3 16h4M17 8h4M17 16h4',
    tv:'M3 5h18v14H3ZM8 22h8M8 1l4 4 4-4',guide:'M3 4h18v17H3ZM3 9h18M9 9v12M9 15h12',
    star:'m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.3l-5.6 2.9 1.1-6.2L3 9.6l6.2-.9Z',
    info:'M12 11v6M12 7h.01M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0',
    refresh:'M20 7v5h-5M4 17v-5h5M5 8a8 8 0 0 1 13-3l2 3M4 16l2 3a8 8 0 0 0 13-3',
    close:'m6 6 12 12M6 18 18 6'
  };
  const icon = name => `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="${paths[name] || paths.film}"/></svg>`;
  const data = window.TV_CATALOG;
  // m1 has a verified title/artwork mismatch. Do not relabel its poster or use it here.
  const movies = data.movies.filter(x => x.id !== 'm1').map(x => ({...x, kind:'movie'}));
  const series = data.series.map(x => ({...x, kind:'series'}));
  const channels = data.channels.map(x => ({id:x.id,title:x.name,logo:x.logo,category:x.category,kind:'live'}));
  const all = [...movies,...series,...channels];
  const byId = id => all.find(x => x.id === id);
  const translations = {
    m2:{genre:'Macera, Dram, Bilim kurgu',description:'İnsanlığın geleceği için bir grup kâşif, galaksinin ötesine uzanan bir yolculuğa çıkar.'},
    m3:{description:'Rüyalardan sır çalan Cobb, bu kez bir fikri zihne yerleştirmek zorundadır. Son bir görev, ona hayatını geri verebilir.'},
    m4:{description:'Bir otelin efsanevi konsiyerji ve genç yardımcısı; kayıp bir tablo, büyük bir miras ve sıra dışı bir maceranın peşindedir.'},
    m5:{description:'Paul Atreides, ailesinin ve halkının geleceği için evrenin en tehlikeli gezegenine gider.'}
  };
  const genre = e => translations[e.id]?.genre || e.genre || e.category || '';
  const description = e => translations[e.id]?.description || e.description || 'Bu içerik için açıklama bulunmuyor.';
  const duration = e => e.durationVerified && e.duration ? `${Math.floor(e.duration/3600)} sa ${Math.round(e.duration%3600/60)} dk` : '';
  const metadata = e => `<div class="metadata">${[e.year, genre(e).split(',').slice(0,2).join(' · '), duration(e)].filter(Boolean).map(x=>`<span>${esc(x)}</span>`).join('')}</div>`;
  const favorites = new Set();
  const routeLabels = {home:'Ana sayfa',live:'Canlı TV',movies:'Filmler',series:'Diziler',favorites:'Favoriler'};
  const allowed = Object.keys(routeLabels).concat('detail','search');
  const params = new URLSearchParams(location.search);
  let route = allowed.includes(params.get('screen')) ? params.get('screen') : 'home';
  let current = byId(params.get('id')) || byId('m2'), filter='Tümü', query='', state='normal', reduced=false, modal=false;
  let history=[], lastFocus='', modalReturn='', returnRoute='home';
  if(route==='detail') returnRoute=current.kind==='series'?'series':'movies';
  if(params.get('capture')==='1') document.body.classList.add('capture');
  function button(label, action, {key='',cls='',ico='',extra=''}={}) { return `<button class="focusable ${cls || 'action'}" data-action="${action}" data-key="${esc(key || action)}" ${extra}>${ico?icon(ico):''}<span>${esc(label)}</span></button>`; }
  function nav() {
    const active = route === 'detail' ? returnRoute : route;
    return `<header class="topbar"><div class="brand"><span class="brand-mark">›</span>IPTV</div><nav class="tabs" aria-label="Ana gezinme">${Object.entries(routeLabels).map(([k,v])=>button(v,`route:${k}`,{cls:'nav-tab',key:`nav:${k}`,extra:active===k?'aria-current="page"':''})).join('')}</nav><div class="nav-tools">${button('', 'route:search',{cls:'icon-button',key:'nav:search',ico:'search',extra:'aria-label="Ara"'})}${button('','settings',{cls:'icon-button',key:'nav:settings',ico:'settings',extra:'aria-label="Ayarlar"'})}</div></header>`;
  }
  function artwork(e, landscape=false) {
    const path=landscape?e.backdrop:e.poster;
    if(state==='missing'||!path) return `<div class="art-fallback">${icon('film')}<span>${esc(e.title)}</span></div>`;
    return `<img src="${esc(path)}" alt="" loading="eager">`;
  }
  function card(e, landscape=false) {return `<button class="focusable poster-card" data-action="detail:${e.id}" data-key="card:${e.id}" aria-label="${esc(e.title)}"><div class="art">${artwork(e,landscape)}${favorites.has(e.id)?`<span class="badge">${icon('check')}Favori</span>`:''}</div><span class="card-label">${esc(e.title)}</span>${landscape?'':`<span class="card-meta">${esc(e.year)} · ${esc(genre(e).split(',')[0])}</span>`}</button>`;}
  function channelCard(e) {return `<button class="focusable channel-card" data-key="channel:${e.id}" data-action="live:${e.id}" aria-label="${esc(e.title)}"><div class="channel-logo">${state==='missing'?icon('tv'):`<img src="${esc(e.logo)}" alt="">`}</div><span class="channel-label">${esc(e.title)}${icon('play')}</span><span class="channel-category">${esc(e.category)}</span></button>`;}
  function home() {
    const e=byId('m2');
    return `<section class="hero">${state==='missing'?'':`<img class="hero-art" src="${e.backdrop}" alt="">`}<div class="hero-content"><p class="eyebrow">Film kütüphanenden</p><h1>${e.title}</h1>${metadata(e)}<p class="hero-description">${description(e)}</p><div class="actions">${button('Ayrıntıları gör',`detail:${e.id}`,{key:'hero',cls:'action primary',ico:'info'})}${button('Canlı TV','route:live',{cls:'action subtle',ico:'tv'})}</div></div><span class="hero-caption">Kütüphanenden bir seçim</span></section>
    <section class="shelf"><div class="shelf-heading"><h2>Film kütüphanen</h2>${button('Tümünü gör','route:movies',{cls:'text-action',ico:'right'})}</div><div class="shelf-row">${['m3','m5','m4','m6'].map(id=>card(byId(id),true)).join('')}</div></section>
    <section class="shelf"><div class="shelf-heading"><h2>Dizi kütüphanen</h2>${button('Tümünü gör','route:series',{cls:'text-action',ico:'right'})}</div><div class="shelf-row">${series.map(e=>card(e,true)).join('')}</div></section>
    <section class="shelf"><div class="shelf-heading"><h2>Canlı kanallar</h2>${button('Tümünü gör','route:live',{cls:'text-action',ico:'right'})}</div><div class="channel-grid">${channels.map(channelCard).join('')}</div></section>`;
  }
  function title(label,sub,action=''){return `<div class="page-title"><div><h1>${label}</h1><p>${sub}</p></div>${action}</div>`;}
  function chips(options){return `<div class="chips" aria-label="Kategori">${options.map(v=>button(v,`filter:${v}`,{key:`filter:${v}`,cls:'chip',ico:filter===v?'check':'',extra:`aria-pressed="${filter===v}"`})).join('')}</div>`;}
  const norm=s=>s.toLocaleLowerCase('tr').replace(/[-\s]/g,'');
  function filtered(list){return list.filter(e=>filter==='Tümü'||norm(genre(e)).includes(norm(filter)));}
  function library() {
    const list=route==='movies'?movies:series;
    const types=route==='movies'?['Tümü','Bilim kurgu','Macera','Aksiyon','Komedi']:['Tümü','Dram','Suç','Gizem','Belgesel'];
    const entries=filtered(list);
    return title(routeLabels[route],`${list.length} ${route==='movies'?'film':'dizi'} · Kütüphanen`)+chips(types)+(entries.length?`<div class="catalog-grid">${entries.map(e=>card(e)).join('')}</div>`:stateBlock('empty','Bu kategoride içerik yok','Başka bir kategori seçerek kütüphanene göz atabilirsin.'));
  }
  function live(){const entries=channels.filter(e=>filter==='Tümü'||e.category===filter);return title('Canlı TV','Kanalların, tek bir yerde.',button('Yayın rehberi','guide',{cls:'action small',ico:'guide'}))+chips(['Tümü','Genel','Haber','Spor','Belgesel','Müzik'])+`<div class="channel-grid">${entries.map(channelCard).join('')}</div><p class="live-note">${icon('info')}Program bilgisi olmayan kanalları da izleyebilirsin.</p>`;}
  function detail(){const e=current;return `${state==='missing'?'':`<img class="detail-bg" src="${e.backdrop}" alt="">`}<div class="detail-back">${button('Geri','back',{cls:'text-action',ico:'back'})}</div><div class="detail-main"><p class="eyebrow">${e.kind==='series'?'Dizi':'Film'}</p><h1>${esc(e.title)}</h1>${metadata(e)}<p class="detail-description">${esc(description(e))}</p><div class="actions">${button(e.kind==='series'?'İlk bölümü oynat':'Oynat','play',{key:'play',cls:'action primary',ico:'play'})}${button(favorites.has(e.id)?'Favorilerde':'Favorilere ekle',`favorite:${e.id}`,{key:'favorite',ico:favorites.has(e.id)?'check':'plus'})}</div><div class="detail-tags">${genre(e).split(',').map(t=>`<span>${esc(t.trim())}</span>`).join('')}</div></div>${e.kind==='series'?`<section class="episodes"><h2>1. sezon</h2><div class="shelf-row">${e.seasons[0].episodes.map(ep=>`<button class="focusable poster-card" data-key="episode:${ep.id}" data-action="play"><div class="art"><img src="${esc(ep.still)}" alt=""></div><span class="card-label">${esc(ep.title.split(' - ').slice(2).join(' - ')||`${ep.number}. Bölüm`)}</span><span class="card-meta">${Math.round(ep.duration/60)} dk</span></button>`).join('')}</div></section>`:''}`;}
  function stateBlock(type,heading,body){return `<section class="state-block"><div class="state-symbol">${icon(type==='error'?'refresh':route==='favorites'?'star':'film')}</div><h2>${heading}</h2><p>${body}</p><div class="actions">${button(type==='error'?'Yeniden dene':state==='empty'?'Kaynağı kontrol et':'Kütüphaneye göz at',type==='error'?'retry':state==='empty'?'source-preview':'route:movies',{cls:'action primary',ico:type==='error'?'refresh':'right'})}</div></section>`;}
  function search(){const entries=query.trim()?all.filter(e=>norm(e.title).includes(norm(query))):[];return `<div class="search-heading">${title('Ara','Film, dizi ya da kanal bul.')}</div><label class="search-field">${icon('search')}<input class="focusable" data-key="query" id="query" aria-label="İçerik ara" placeholder="Ne izlemek istersin?" value="${esc(query)}" autocomplete="off"></label><div id="search-results">${query.trim()?(entries.length?`<div class="catalog-grid">${entries.map(e=>e.kind==='live'?channelCard(e):card(e)).join('')}</div>`:stateBlock('empty','Sonuç bulunamadı','Farklı bir adla yeniden arayabilirsin.')):''}</div>`;}
  function favoritesView(){const list=all.filter(e=>favorites.has(e.id));return title('Favoriler','Sevdiğin içerikler bir arada.')+(list.length?`<div class="catalog-grid">${list.map(e=>card(e)).join('')}</div>`:stateBlock('empty','İzlemek istediklerine yer aç','Bir filmin veya dizinin ayrıntılarından favorilerine ekleyebilirsin.'));}
  function draw(focusKey,scroll=0){
    let body;
    if(state==='loading')body=title(routeLabels[route]||'Kütüphanen','')+`<p class="loading-copy">Kütüphanen hazırlanıyor…</p><div class="skeleton-grid" aria-hidden="true">${Array(5).fill('<div class="skeleton"></div>').join('')}</div>`;
    else if(state==='empty')body=stateBlock('empty','Kütüphanen için yer hazır','İçerikler eklendiğinde burada görünecek. Kayıtlı kaynağını kontrol edebilirsin.');
    else if(state==='error')body=stateBlock('error','Şu anda yüklenemedi','Kayıtlı bilgilerin korunuyor. Bağlantını kontrol edip yeniden deneyebilirsin.');
    else body=({home,live,movies:library,series:library,detail,search,favorites:favoritesView}[route])();
    app.innerHTML=nav()+`<main class="content ${route==='detail'?'detail-content':''}" aria-label="${routeLabels[route]||'İçerik'}">${body}</main>`;
    const c=app.querySelector('.content');c.addEventListener('scroll',()=>c.classList.toggle('scrolled',c.scrollTop>0),{passive:true});c.scrollTop=scroll;c.classList.toggle('scrolled',c.scrollTop>0);
    const preferred=route==='home'?'hero':route==='detail'?'play':route==='search'?'query':null;
    const initial=preferred&&app.querySelector(`[data-key="${preferred}"]`)?preferred:app.querySelector('.content .poster-card,.content .channel-card')?.dataset.key || app.querySelector('.content .focusable')?.dataset.key || `nav:${route}`;
    requestAnimationFrame(()=>focusByKey(focusKey || initial,false));
  }
  function focusByKey(key,scroll=true){const el=[...document.querySelectorAll('#tv [data-key]')].find(x=>x.dataset.key===key);if(!el)return;document.querySelectorAll('#tv .is-focused').forEach(x=>x.classList.remove('is-focused'));el.classList.add('is-focused');lastFocus=key;el.focus({preventScroll:true});if(scroll)ensureVisible(el);}
  function ensureVisible(el){const content=el.closest('.content');if(!content)return;const r=el.getBoundingClientRect(),p=content.getBoundingClientRect(),s=Number(tv.style.getPropertyValue('--scale'))||1;const lower=p.bottom-24*s,upper=p.top+32*s;if(r.bottom>lower)content.scrollTop+=(r.bottom-lower)/s;if(r.top<upper)content.scrollTop-=(upper-r.top)/s;}
  function save(){return {route,current:current.id,filter,query,key:lastFocus,scroll:app.querySelector('.content')?.scrollTop||0};}
  function go(next,id){history.push(save());if(next==='detail'){returnRoute=route;current=byId(id);}route=next;filter='Tümü';state='normal';$('#review-state').value=state;draw();}
  function back(){if(modal){closeModal();return;}const prev=history.pop();if(prev){route=prev.route;current=byId(prev.current);filter=prev.filter;query=prev.query;draw(prev.key,prev.scroll);}else if(route==='detail'){route=returnRoute;draw(`card:${current.id}`);}else{focusByKey(`nav:${route}`);}}
  function notify(text){$('#review-note').textContent=text;}
  function openModal(type,e){modalReturn=lastFocus;modal=true;app.inert=true;const isSettings=type==='settings', isLive=type==='live';overlay.innerHTML=`<div class="overlay-shade"><section class="sheet" role="dialog" aria-modal="true" aria-labelledby="sheet-title"><div class="sheet-icon">${icon(isSettings?'settings':isLive?'tv':'guide')}</div><h2 id="sheet-title">${isSettings?'Ayarlar':isLive?esc(e.title):'Program bilgisi bulunamadı'}</h2><p>${isSettings?'Görünümü izleme alışkanlığına göre düzenle.':isLive?'Bu kanal için yayın rehberi bilgisi bulunmuyor.':'Bu kaynak için program bilgisi henüz yok. Canlı kanallara göz atabilirsin.'}</p>${isSettings?`<div class="setting-row"><span>Hareketi azalt</span><button class="focusable switch" data-key="motion" data-action="motion" role="switch" aria-label="Hareketi azalt" aria-checked="${reduced}"></button></div><p class="description">Kart büyümesi ve geçiş hareketleri azaltılır.</p>`:''}${isLive?button('Canlı izle','play',{cls:'action primary',ico:'play'}):''}${button(isSettings?'Bitti':'Kanallara dön','close',{key:'sheet-close',cls:'action',ico:'back'})}</section></div>`;requestAnimationFrame(()=>focusByKey(isSettings?'motion':'sheet-close'));}
  function closeModal(){modal=false;overlay.innerHTML='';app.inert=false;focusByKey(modalReturn,false);}
  function act(action){const [kind,...rest]=action.split(':'),value=rest.join(':');
    if(kind==='route')go(value);
    else if(kind==='detail')go('detail',value);
    else if(kind==='filter'){filter=value;draw(`filter:${value}`);}
    else if(kind==='favorite'){favorites.has(value)?favorites.delete(value):favorites.add(value);draw('favorite',app.querySelector('.content').scrollTop);notify('Favori seçimi bu tasarım oturumunda tutulur.');}
    else if(kind==='back')back();
    else if(kind==='close')closeModal();
    else if(kind==='settings'||kind==='guide')openModal(kind);
    else if(kind==='live')openModal('live',byId(value));
    else if(kind==='play')notify('Oynat eylemi seçildi · Bu görsel taslak gerçek yayın açmaz. Android uygulamasını canlı kumanda sekmesinde test edebilirsin.');
    else if(kind==='source-preview')notify('Kaynak yönetimi mevcut tam akış prototipinde bulunuyor. Bu taslak ana gezinme ve kütüphane görsellerini incelemek içindir.');
    else if(kind==='motion'){reduced=!reduced;tv.classList.toggle('reduced',reduced);overlay.querySelector('[role=switch]').setAttribute('aria-checked',String(reduced));}
    else if(kind==='retry'){state='normal';$('#review-state').value=state;draw();}
  }
  tv.addEventListener('click',e=>{const el=e.target.closest('[data-action]');if(el){lastFocus=el.dataset.key;act(el.dataset.action);}});
  tv.addEventListener('error',e=>{if(e.target.tagName==='IMG'){e.target.hidden=true;e.target.parentElement.classList.add('failed');}},true);
  tv.addEventListener('focusin',e=>{if(e.target.matches('.focusable')){document.querySelectorAll('#tv .is-focused').forEach(x=>x.classList.remove('is-focused'));e.target.classList.add('is-focused');lastFocus=e.target.dataset.key;}});
  tv.addEventListener('input',e=>{if(e.target.id==='query'){query=e.target.value;const cursor=e.target.selectionStart;draw('query');requestAnimationFrame(()=>{const input=$('#query');input?.setSelectionRange(cursor,cursor);});}});
  function key(key){if(key==='Escape'){back();return;}const scope=modal?overlay:app;let active=[...scope.querySelectorAll('.focusable')].find(e=>e.dataset.key===lastFocus);if(!active){focusByKey(scope.querySelector('.focusable')?.dataset.key);return;}if(key==='Enter'){if(active.dataset.action)act(active.dataset.action);return;}const dirs={ArrowLeft:[-1,0],ArrowRight:[1,0],ArrowUp:[0,-1],ArrowDown:[0,1]},dir=dirs[key];if(!dir)return;
    const a=active.getBoundingClientRect(),ac={x:a.left+a.width/2,y:a.top+a.height/2};let best=null,bestScore=Infinity;
    for(const el of scope.querySelectorAll('.focusable')){if(el===active||el.disabled||el.closest('[inert]'))continue;const r=el.getBoundingClientRect();if(!r.width||!r.height)continue;const dx=r.left+r.width/2-ac.x,dy=r.top+r.height/2-ac.y,primary=dx*dir[0]+dy*dir[1],cross=Math.abs(dir[0]?dy:dx);if(primary<=1)continue;const rowOverlap=dir[0]?Math.min(a.bottom,r.bottom)-Math.max(a.top,r.top)>0:Math.min(a.right,r.right)-Math.max(a.left,r.left)>0;if(dir[0]&&!rowOverlap)continue;const score=primary+cross*3+(rowOverlap?0:500);if(score<bestScore){best=el;bestScore=score;}}
    if(best)focusByKey(best.dataset.key);
  }
  document.addEventListener('keydown',e=>{if(!e.key.startsWith('Arrow')&&!['Enter','Escape'].includes(e.key))return;if(e.target.closest('.review-tools,.review-footer'))return;if(e.target.id==='query'&&['ArrowLeft','ArrowRight','Enter'].includes(e.key))return;e.preventDefault();key(e.key);});
  document.querySelectorAll('[data-remote]').forEach(b=>b.addEventListener('click',()=>key(b.dataset.remote)));
  $('#review-state').addEventListener('change',e=>{if(modal)closeModal();state=e.target.value;draw();});
  $('#fullscreen').addEventListener('click',()=>{document.body.requestFullscreen?.().catch(()=>notify('Tam ekran açılamadı. Önizleme panelini genişleterek inceleyebilirsin.'));});
  function resize(){const stage=$('#stage');tv.style.setProperty('--scale',Math.min(stage.clientWidth/960,stage.clientHeight/540));}
  new ResizeObserver(resize).observe($('#stage'));resize();draw();
})();
