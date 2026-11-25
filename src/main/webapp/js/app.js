'use strict';
const context=document.querySelector('meta[name="context-path"]').content;
const csrf=document.querySelector('meta[name="csrf-token"]').content;
const toast=(message)=>{const node=document.getElementById('toast');if(!node)return;node.textContent=message;node.classList.add('visible');clearTimeout(window.toastTimer);window.toastTimer=setTimeout(()=>node.classList.remove('visible'),4200);};

async function action(data){
  const response=await fetch(context+'/api/action',{method:'POST',headers:{'X-CSRF-Token':csrf},body:new URLSearchParams(data)});
  if(response.status===401){
    location.href=context+'/login?next='+encodeURIComponent(location.pathname.substring(context.length)+location.search);
    throw Error('Please log in to continue.');
  }
  const result=await response.json();
  if(!response.ok)throw Error(result.error||'Please try again.');
  return result;
}

// Cover image failure handler with naturalWidth verification
function bindCoverImages(root=document){
  root.querySelectorAll('img.book-cover').forEach(img=>{
    const fail=()=>img.classList.add('failed');
    img.addEventListener('error',fail);
    img.addEventListener('load',()=>{if(img.naturalWidth<5)fail();});
    if(img.complete&&(img.naturalWidth<5))fail();
  });
}
bindCoverImages();

const menu=document.getElementById('menu-toggle'),nav=document.getElementById('nav');
function closeMenu(){if(!nav||!menu)return;nav.classList.remove('open');menu.setAttribute('aria-expanded','false');}
if(menu&&nav){
  menu.addEventListener('click',()=>{const open=nav.classList.toggle('open');menu.setAttribute('aria-expanded',String(open));menu.setAttribute('aria-label',open?'Close navigation':'Open navigation');});
  document.addEventListener('keydown',e=>{if(!nav.classList.contains('open'))return;if(e.key==='Escape'){closeMenu();menu.focus();}if(e.key==='Tab'){const nodes=[menu,...nav.querySelectorAll('a,button,summary')].filter(n=>n.offsetParent!==null);const first=nodes[0],last=nodes.at(-1);if(e.shiftKey&&document.activeElement===first){last.focus();e.preventDefault();}else if(!e.shiftKey&&document.activeElement===last){first.focus();e.preventDefault();}}});
  window.addEventListener('resize',()=>{if(window.innerWidth>1150)closeMenu();});
}

const dialog=document.getElementById('confirm-dialog');
function confirmAction(message){
  return new Promise(resolve=>{
    if(!dialog){resolve(window.confirm(message));return;}
    document.getElementById('confirm-text').textContent=message;
    dialog.showModal();
    let settled=false;
    const done=value=>{if(settled)return;settled=true;dialog.close();resolve(value);};
    document.getElementById('confirm-ok').onclick=()=>done(true);
    document.getElementById('confirm-cancel').onclick=()=>done(false);
    dialog.oncancel=e=>{e.preventDefault();done(false);};
  });
}

document.querySelectorAll('form[data-confirm-native]').forEach(form=>form.addEventListener('submit',async e=>{e.preventDefault();if(await confirmAction(form.dataset.confirmNative))form.submit();}));

// Unified Action Button Handler (Shelf, Bookmark, Save, Remove)
document.addEventListener('click', async e => {
  const button = e.target.closest('[data-action]');
  if(!button) return;
  button.disabled = true;
  try {
    let verb = button.dataset.action;
    let data = { action: verb, bookId: button.dataset.book };
    if(verb === 'begin') data = { action: 'save', bookId: button.dataset.book, status: 'CURRENTLY_READING', page: '0' };
    if(verb === 'remove' && !await confirmAction('Remove this book and its private notes from your collection?')) return;
    
    await action(data);

    if(verb === 'bookmark') {
      const isPressed = button.getAttribute('aria-pressed') === 'true';
      button.setAttribute('aria-pressed', String(!isPressed));
      toast(isPressed ? 'Bookmark removed.' : 'Bookmark saved to shelf.');
    } else if(verb === 'remove') {
      toast('Removed from your collection.');
      location.reload();
    } else if(verb === 'shelf') {
      toast('Saved to your collection (Want to Read).');
      // Update all matching shelf buttons on page for this book
      document.querySelectorAll(`[data-action="shelf"][data-book="${button.dataset.book}"]`).forEach(btn => {
        btn.classList.add('shelved');
        if(btn.classList.contains('shelf-chip')) {
          btn.textContent = '✓ SHELVED';
        } else if(btn.classList.contains('action-btn-add')) {
          btn.textContent = '✓ In Collection';
        } else {
          btn.textContent = '✓ In My Collection';
        }
      });
    } else {
      toast(verb === 'begin' ? 'Your next chapter begins. Added to Currently Reading.' : 'Sanctuary updated.');
    }
  } catch(err) {
    toast(err.message || 'Action failed.');
  } finally {
    button.disabled = false;
  }
});

// Ajax forms (ratings, progress, settings)
document.querySelectorAll('form[data-ajax]').forEach(form=>form.addEventListener('submit',async e=>{
  e.preventDefault();
  const button=form.querySelector('button[type="submit"],button:not([type])');
  if(form.dataset.confirm&&!await confirmAction(form.dataset.confirm))return;
  if(button)button.disabled=true;
  try{
    const result=await action(new FormData(form));
    toast(result.message);
    if(form.dataset.redirect)location.href=context+form.dataset.redirect;
    else if(form.dataset.reload!==undefined)location.reload();
  }catch(error){
    toast(error.message);
  }finally{
    if(button)button.disabled=false;
  }
}));

// Password validation
document.querySelectorAll('form').forEach(form=>{
  if(!form.elements.confirmPassword)return;
  form.addEventListener('submit',e=>{
    if(form.elements.password.value!==form.elements.confirmPassword.value){
      e.preventDefault();
      toast('Passwords do not match.');
      form.elements.confirmPassword.focus();
    }
  });
});

// Shelf slider
const shelf=document.getElementById('shelf');
if(shelf){
  const prevBtn=document.getElementById('shelf-prev'),nextBtn=document.getElementById('shelf-next');
  if(prevBtn)prevBtn.onclick=()=>shelf.scrollBy({left:-shelf.clientWidth*.75,behavior:'smooth'});
  if(nextBtn)nextBtn.onclick=()=>shelf.scrollBy({left:shelf.clientWidth*.75,behavior:'smooth'});
  shelf.addEventListener('keydown',e=>{if(['ArrowRight','ArrowLeft'].includes(e.key)){e.preventDefault();shelf.scrollBy({left:e.key==='ArrowRight'?200:-200,behavior:'smooth'});}});
}

// Dust canvas particles
const reduced=matchMedia('(prefers-reduced-motion: reduce)'),canvas=document.getElementById('dust');
if(canvas){
  const ctx=canvas.getContext('2d');
  let dustFrame=0;
  const motes=Array.from({length:38},()=>({x:Math.random(),y:Math.random(),r:Math.random()*1.1+.3,v:Math.random()*.00006+.000015}));
  function resizeDust(){canvas.width=innerWidth;canvas.height=innerHeight;}
  resizeDust();addEventListener('resize',resizeDust);
  function drawDust(){
    ctx.clearRect(0,0,canvas.width,canvas.height);
    ctx.fillStyle='#d6ae63';
    for(const p of motes){
      p.y-=p.v;if(p.y<0)p.y=1;
      ctx.globalAlpha=.15+.4*Math.sin(p.x*8+p.y*7)**2;
      ctx.beginPath();ctx.arc(p.x*canvas.width,p.y*canvas.height,p.r,0,Math.PI*2);ctx.fill();
    }
    dustFrame=requestAnimationFrame(drawDust);
  }
  function manageDust(){cancelAnimationFrame(dustFrame);if(!document.hidden&&!reduced.matches)drawDust();else ctx.clearRect(0,0,canvas.width,canvas.height);}
  document.addEventListener('visibilitychange',manageDust);reduced.addEventListener('change',manageDust);manageDust();
}

// Synthesized soundscape
const sound=document.getElementById('soundscape');
if(sound){
  let audio=null,master=null,soundOn=false;
  try{if(localStorage.getItem('soundscape')==='true')sound.title='Previously enabled. Click to resume your soundscape.';}catch{}
  if(!window.AudioContext&&!window.webkitAudioContext){sound.disabled=true;sound.title='Web Audio is unavailable in this browser.';}
  else sound.addEventListener('click',async()=>{
    try{
      if(!audio){
        audio=new(window.AudioContext||window.webkitAudioContext)();
        master=audio.createGain();master.gain.value=.09;master.connect(audio.destination);
        [65.41,98.0].forEach(f=>{
          const oscillator=audio.createOscillator(),gain=audio.createGain();
          oscillator.frequency.value=f;gain.gain.value=.08;
          oscillator.connect(gain).connect(master);oscillator.start();
        });
        const buffer=audio.createBuffer(1,audio.sampleRate*6,audio.sampleRate),samples=buffer.getChannelData(0);
        for(let i=0;i<samples.length;i++){
          const crack=Math.random()>.998?1:.06;const rustle=.05*(1+Math.sin(i/audio.sampleRate*1.1));
          samples[i]=(Math.random()*2-1)*(crack+rustle);
        }
        const source=audio.createBufferSource(),filter=audio.createBiquadFilter();
        source.buffer=buffer;source.loop=true;filter.type='lowpass';filter.frequency.value=1100;
        source.connect(filter).connect(master);source.start();
      }
      soundOn=!soundOn;
      if(soundOn)await audio.resume();else await audio.suspend();
      sound.setAttribute('aria-pressed',String(soundOn));
      sound.querySelector('span').textContent='SOUNDSCAPE · '+(soundOn?'ON':'OFF');
      try{localStorage.setItem('soundscape',String(soundOn));}catch{}
    }catch{toast('Soundscape could not start in this browser.');}
  });
}

// Chat assistant
const chatForm=document.getElementById('chat-form');
if(chatForm){
  const history=document.getElementById('chat-history'),send=chatForm.querySelector('button'),input=chatForm.querySelector('input');
  let pending=false;
  const bubble=(text,user=false)=>{
    const p=document.createElement('p');p.className='chat-message'+(user?' user':'');p.textContent=text;
    history.append(p);history.scrollTop=history.scrollHeight;return p;
  };
  chatForm.addEventListener('submit',async e=>{
    e.preventDefault();
    if(pending||!input.value.trim())return;
    const text=input.value.trim();bubble(text,true);input.value='';
    const waiting=bubble('The Librarian is consulting the shelves…');
    send.disabled=true;pending=true;
    try{
      const response=await fetch(context+'/api/chat',{method:'POST',headers:{'X-CSRF-Token':csrf},body:new URLSearchParams({message:text})});
      const data=await response.json();
      waiting.textContent=data.reply||data.error||'Please try again.';
    }catch{waiting.textContent='The connection was interrupted. Please try again.';}
    finally{send.disabled=false;pending=false;input.focus();}
  });
  const resetBtn=document.getElementById('new-conversation');
  if(resetBtn)resetBtn.onclick=async()=>{
    if(pending)return;
    try{
      const response=await fetch(context+'/api/chat',{method:'POST',headers:{'X-CSRF-Token':csrf},body:new URLSearchParams({reset:'true'})});
      if(!response.ok)throw Error();
      while(history.children.length>1)history.lastChild.remove();
      toast('A new conversation begins.');
    }catch{toast('Could not reset the conversation.');}
  };
}

/* ==========================================================================
   INTERACTIVE BOOK DETAILS MODAL & CLIENT DISCOVER CATALOGUE
   ========================================================================== */

const bookModal = document.getElementById('book-detail-modal');
let catalogueData = [];
const catScript = document.getElementById('catalogue-data');
if (catScript) {
  try {
    catalogueData = JSON.parse(catScript.textContent);
  } catch(e) {
    console.error('Failed to parse catalogue data', e);
  }
}

function openBookModal(bookId) {
  if (!bookModal || !catalogueData.length) return;
  const book = catalogueData.find(b => String(b.id) === String(bookId));
  if (!book) return;

  // Fallback and cover
  const modalFallback = document.getElementById('modal-fallback');
  if (modalFallback) {
    modalFallback.setAttribute('data-genre', book.genre || '');
    document.getElementById('modal-fallback-title').textContent = book.title || '';
    document.getElementById('modal-fallback-author').textContent = book.author || '';
    document.getElementById('modal-fallback-genre').textContent = book.genre || '';
  }

  const modalCoverImg = document.getElementById('modal-cover-img');
  if (modalCoverImg) {
    modalCoverImg.classList.remove('failed');
    if (book.cover_image_url) {
      modalCoverImg.src = book.cover_image_url;
      modalCoverImg.alt = `${book.title} by ${book.author}`;
    } else {
      modalCoverImg.classList.add('failed');
    }
  }

  // Genre, Title, Author
  const modalGenreBadge = document.getElementById('modal-genre-badge');
  if (modalGenreBadge) {
    modalGenreBadge.textContent = book.genre || '';
    modalGenreBadge.setAttribute('data-genre', book.genre || '');
  }
  const modalTitle = document.getElementById('modal-book-title');
  if (modalTitle) modalTitle.textContent = book.title || '';
  const modalAuthor = document.getElementById('modal-book-author');
  if (modalAuthor) modalAuthor.textContent = `By ${book.author || ''}`;

  // Metadata
  const modalYear = document.getElementById('modal-year');
  if (modalYear) modalYear.textContent = `Year: ${book.publication_year || 'Archives'}`;
  const modalPages = document.getElementById('modal-pages');
  if (modalPages) modalPages.textContent = book.total_pages || 'Varied';
  const modalLanguage = document.getElementById('modal-language');
  if (modalLanguage) modalLanguage.textContent = book.language || 'English';
  const modalRating = document.getElementById('modal-rating');
  if (modalRating) modalRating.textContent = `${book.avg_rating || 4.6} / 5`;
  const modalReviewsCount = document.getElementById('modal-reviews-count');
  if (modalReviewsCount) modalReviewsCount.textContent = book.rating_count || 120;

  // Tags
  const modalTags = document.getElementById('modal-tags');
  if (modalTags) {
    modalTags.innerHTML = '';
    if (book.tags && Array.isArray(book.tags)) {
      book.tags.forEach(tag => {
        const span = document.createElement('span');
        span.className = 'tag-pill';
        span.textContent = `#${tag}`;
        modalTags.appendChild(span);
      });
    }
  }

  // Synopsis
  const modalDesc = document.getElementById('modal-description');
  if (modalDesc) modalDesc.textContent = book.description || '';

  // Sample Reviews
  const modalReviewsList = document.getElementById('modal-reviews-list');
  if (modalReviewsList) {
    modalReviewsList.innerHTML = '';
    const reviews = book.reviews && book.reviews.length ? book.reviews : [
      { reviewer_name: 'Elena Vance', review_date: 'October 14, 2025', rating: 5, comment: 'A magical, deeply atmospheric journey through shadow and wonder.' },
      { reviewer_name: 'Julian Thorne', review_date: 'August 22, 2025', rating: 4, comment: 'Elegant prose and masterful pacing that transport you to another realm.' }
    ];
    reviews.forEach(rev => {
      const card = document.createElement('article');
      card.className = 'modal-review-card';
      card.innerHTML = `
        <div class="review-author-row">
          <span class="reviewer-glyph">⚜</span>
          <div>
            <strong>${rev.reviewer_name || 'Anonymous Reader'}</strong>
            <span class="review-date">${rev.review_date || 'October 2025'}</span>
          </div>
          <span class="review-stars">★ ${rev.rating || 5} / 5</span>
        </div>
        <p class="review-text">“${rev.comment || ''}”</p>
      `;
      modalReviewsList.appendChild(card);
    });
  }

  // Collection Button state
  const shelfBtn = document.getElementById('modal-shelf-btn');
  if (shelfBtn) {
    shelfBtn.dataset.book = book.id;
    const cardBtn = document.querySelector(`.action-btn-add[data-book="${book.id}"].shelved, .shelf-chip[data-book="${book.id}"].shelved`);
    if (cardBtn) {
      shelfBtn.textContent = '✓ In My Collection';
      shelfBtn.classList.add('shelved');
    } else {
      shelfBtn.textContent = '+ Add to My Collection';
      shelfBtn.classList.remove('shelved');
    }
  }

  // Full page link
  const fullPageLink = document.getElementById('modal-full-page-link');
  if (fullPageLink) {
    fullPageLink.href = `${context}/book?id=${book.id}`;
  }

  bindCoverImages(bookModal);
  bookModal.showModal();
}

// Modal open triggers
document.addEventListener('click', e => {
  const trigger = e.target.closest('[data-view-modal], [data-modal-book]');
  if (trigger) {
    e.preventDefault();
    const bookId = trigger.dataset.viewModal || trigger.dataset.modalBook;
    openBookModal(bookId);
  }
});

// Modal close triggers
const modalCloseBtn = document.getElementById('modal-close');
if (modalCloseBtn && bookModal) {
  modalCloseBtn.addEventListener('click', () => bookModal.close());
  bookModal.addEventListener('click', e => {
    if (e.target === bookModal) bookModal.close();
  });
}
