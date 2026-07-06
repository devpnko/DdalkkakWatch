/* 딸깍 온보딩 마법사 — Hermes 패턴 적용 4-step state machine
   - 환영: Quick(C1 빠른 시작) vs Full(시나리오 추천)
   - 시나리오: 3 질문 (env / have / prio)
   - 추천: 6 path 중 최적 1개 + 비교표
   - 확인: 5-step 체크리스트 + 트러블슈팅
*/

const PATHS = {
  c1: {
    id: 'C1', title: 'Mac 내장 마이크 + 워치 PTT',
    tag: '✅ 지원',
    blurb: '추가 장비 없이 가장 빠른 셋업. 데스크에서 Mac 1m 이내일 때 가장 좋음.',
    steps: [
      '워치에 <code>DdalkkakWatch.apk</code> 설치 (adb install)',
      '워치 앱 실행 → 화면 한 번 탭 → Mac BT 페어링 (2분 안에)',
      'Mac에서 <code>OpenTypeless / Superwhisper</code> 단축키를 <b>Opt+Cmd+X</b>로 설정',
      'Mac 마이크는 시스템 설정 → 사운드 → 입력에서 <b>MacBook Pro 마이크</b> 또는 외장',
      '아무 텍스트 필드에 커서 → 워치 꾹 누름 → 말하기'
    ],
    why: '0원, 3분, 1m 이내면 Mac 내장 mic가 충분히 잘 잡습니다.',
    file: 'paths/c1.html'
  },
  c2: {
    id: 'C2', title: 'Galaxy Buds / AirPods + 워치 PTT',
    tag: '✅ 지원',
    blurb: '이어폰을 Mac에 페어링해서 Mac 마이크로 쓰는 path. 워치는 PTT 트리거만.',
    steps: [
      'C1 셋업 먼저 완료',
      'Galaxy Buds (또는 AirPods)를 <b>Mac에</b> BT 페어링',
      'Mac 사운드 → 입력 → <b>Galaxy Buds</b> 선택 (HFP 모드 자동)',
      'OpenTypeless / Superwhisper 마이크 입력도 Galaxy Buds로 변경',
      '워치 꾹 누름 → 이어폰 마이크로 말함 → Mac에 텍스트'
    ],
    why: '이어폰 마이크가 입에 가까워 잡음 적음. 어디서나 (Mac에 BT만 닿으면) 가능.',
    file: 'paths/c2.html'
  },
  c3: {
    id: 'C3', title: 'Lark M2 / DJI mic + 워치 PTT',
    tag: '💡 권고',
    blurb: '유튜브 녹화·회의 등 음질이 진짜 중요한 경우. USB-C 외장 마이크.',
    steps: [
      'C1 셋업 먼저 완료',
      'Hollyland Lark M2 또는 DJI Mic을 Mac에 USB-C 연결',
      'Mac 사운드 → 입력 → <b>Lark M2 / DJI Mic</b> 선택',
      '음성앱(OpenTypeless 등)의 마이크 입력도 동일하게 변경',
      '워치 꾹 누름 → Lavalier 마이크로 발화'
    ],
    why: '잡음·울림 거의 0. 회의·녹화·인터뷰 등 결과물 품질 영향 큼.',
    file: 'paths/c3.html'
  },
  c4: {
    id: 'C4', title: '워치 마이크 → roc-vad WiFi UDP',
    tag: '🚧 Coming soon',
    blurb: '워치 본질 — 워치 자체 마이크가 Mac의 가상 마이크로. WiFi 같은 망 필요.',
    steps: [
      '<b>아직 개발 중</b> (3.5주 dev 예상)',
      'roc-vad 가상 오디오 드라이버 Mac에 설치',
      'Wear OS roc-sender 앱 (예정)',
      '워치 ↔ Mac 같은 WiFi 또는 USB tethering',
      '워치 꾹 누름 → 워치 mic → UDP → Mac 가상 mic → 받아쓰기'
    ],
    why: '진짜 워치 본질. Mac 멀리(5m+) 있어도 가능. 단 음질 6/10, 지연 ~300ms.',
    file: 'paths/c4.html'
  },
  c5: {
    id: 'C5', title: '워치 → 안드로이드 폰 → Mac',
    tag: '🚧 Coming soon',
    blurb: '갤럭시 폰 항상 휴대하는 사용자용. 폰을 Mac mic 게이트웨이로.',
    steps: [
      '<b>아직 개발 중</b> (3주 dev 예상)',
      '폰에 brigde 앱, Mac에 WebRTC 수신 데몬',
      '워치 PTT → 폰이 mic 캡처 → WebRTC → Mac',
      'Mac 가상 마이크로 라우팅'
    ],
    why: '갤럭시 생태계 안에서 매끄럽고, BT/WiFi 둘 다 가능.',
    file: 'paths/c5.html'
  },
  c6: {
    id: 'C6', title: 'Mac 단축키 단독 (워치 없음)',
    tag: '✅ 지원',
    blurb: '워치가 없거나, 워치 안 차고 싶을 때. Mac 키보드 단축키 1발.',
    steps: [
      'OpenTypeless / Superwhisper 설치',
      '단축키를 본인이 편한 조합으로 (예: <code>Opt+Space</code>)',
      'Mac 사운드 → 입력 → 원하는 마이크',
      '단축키 → 발화'
    ],
    why: '워치 없이도 받아쓰기 자체는 가능. 워치는 "손이 키보드 떠나도" 가치를 주는 path 1~5에서만.',
    file: 'paths/c6.html'
  }
};

/* ── 추천 알고리즘 ─────────────────────
   3 답변 → 가중 점수 → 최고 path. 무난한 규칙 기반 (학습 X).
*/
function recommend({env, have = [], prio}) {
  const s = {c1:0, c2:0, c3:0, c4:0, c5:0, c6:0};

  // 환경
  if (env === 'desk')   { s.c1+=3; s.c2+=2; s.c3+=1; }
  if (env === 'meet')   { s.c3+=4; s.c2+=2; }
  if (env === 'mobile') { s.c4+=3; s.c5+=3; s.c2+=2; }
  if (env === 'any')    { s.c1+=2; s.c2+=2; }

  // 장비
  if (have.includes('buds'))  s.c2+=4;
  if (have.includes('lav'))   s.c3+=4;
  if (have.includes('phone')) s.c5+=3;
  if (have.includes('none'))  { s.c1+=2; s.c6+=2; }

  // 우선순위
  if (prio === 'fast')    { s.c1+=4; s.c6+=2; }
  if (prio === 'quality') { s.c3+=4; s.c2+=2; }
  if (prio === 'purity')  { s.c4+=5; }

  // Coming-soon 패널티 (실제 셋업 가능한 것 우선)
  s.c4 -= 2; s.c5 -= 2;

  // 워치 없는 경우만 C6
  // (env/have 무관하게 C6은 "정말 워치가 없을 때" — 점수 약하게)
  const winnerId = Object.entries(s).sort((a,b)=>b[1]-a[1])[0][0];
  return PATHS[winnerId];
}

/* ── State + DOM ──────────────── */
const state = { step:1, env:null, have:new Set(), prio:null, picked:null };

const $$ = s => document.querySelectorAll(s);
const $  = s => document.querySelector(s);

function showStep(n){
  state.step = n;
  $$('.screen').forEach(el => el.hidden = (+el.dataset.screen !== n));
  $$('.dot').forEach(d => {
    const k = +d.dataset.step;
    d.classList.toggle('on',  k === n);
    d.classList.toggle('done', k <  n);
  });
  $('#restart').hidden = (n === 1);
  window.scrollTo({top:0, behavior:'smooth'});
}

/* STEP 1 — Quick vs Full */
$$('[data-go]').forEach(btn => btn.addEventListener('click', e => {
  const target = e.currentTarget.dataset.go;
  if (target === 'quick') {
    state.picked = PATHS.c1;
    renderRec();
    showStep(3);
  } else if (target === 'full') {
    showStep(2);
  }
}));

/* STEP 2 — 질문 */
$$('.q-opts').forEach(box => {
  const q = box.dataset.q;
  const multi = box.classList.contains('multi');
  box.addEventListener('click', e => {
    const btn = e.target.closest('button'); if (!btn) return;
    const v = btn.dataset.v;

    if (multi) {
      btn.classList.toggle('on');
      if (state.have.has(v)) state.have.delete(v); else state.have.add(v);
    } else {
      [...box.children].forEach(b => b.classList.remove('on'));
      btn.classList.add('on');
      state[q] = v;
    }
    refreshGate();
  });
});

function refreshGate(){
  $('#toRec').disabled = !(state.env && state.prio);
}

$('#toRec').addEventListener('click', () => {
  state.picked = recommend({env:state.env, have:[...state.have], prio:state.prio});
  renderRec();
  showStep(3);
});

/* STEP 3 — 추천 */
function renderRec(){
  const p = state.picked || PATHS.c1;
  $('#rec').innerHTML = `
    <div class="rec-id">추천 · ${p.id} · ${p.tag}</div>
    <h2>${p.title}</h2>
    <p>${p.blurb}</p>
    <ol>${p.steps.map(s => `<li>${s}</li>`).join('')}</ol>
    <p class="why"><b>왜 이 path?</b> ${p.why}</p>
    <p><a href="${p.file}">→ 상세 가이드 (스크린샷 포함)</a></p>
  `;
}

$$('[data-back]').forEach(b => b.addEventListener('click', () => showStep(+b.dataset.back)));
$('#toVerify').addEventListener('click', () => showStep(4));

/* STEP 4 — 트러블 토글 */
$('#trouble').addEventListener('click', e => {
  e.preventDefault();
  $('#troubleshoot').hidden = false;
  $('#troubleshoot').scrollIntoView({behavior:'smooth'});
});

$('#done').addEventListener('click', e => {
  e.preventDefault();
  localStorage.setItem('ddalkkak-onboarded', JSON.stringify({path: state.picked?.id, at: Date.now()}));
  alert(`${state.picked?.id} 셋업 완료 등록. 즐거운 받아쓰기!`);
});

/* Restart */
$('#restart').addEventListener('click', () => {
  state.env = null; state.have.clear(); state.prio = null; state.picked = null;
  $$('.q-opts button.on').forEach(b => b.classList.remove('on'));
  showStep(1);
});

/* 첫 진입 */
showStep(1);
