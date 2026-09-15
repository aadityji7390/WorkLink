document.addEventListener("DOMContentLoaded", async function () {
    const loggedIn=localStorage.getItem("worklinkLoggedIn"), role=localStorage.getItem("worklinkRole"), phone=localStorage.getItem("worklinkPhone"), token=localStorage.getItem("worklinkToken");
    if(loggedIn!=="true"||role!=="worker"||!phone){location.href="login.html";return;}
    const API=window.API_BASE||location.origin;
    const headers=token?{Authorization:"Bearer "+token}:{};
    const $=id=>document.getElementById(id);
    function esc(v){return String(v??"").replace(/[&<>"']/g,m=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[m]));}
    try{const r=await fetch(API+"/api/worker/profile?phone="+encodeURIComponent(phone),{headers});const d=await r.json();if(d.success){const n=d.name||"Worker";$('dashboardName').textContent=n;$('welcomeName').textContent=n;$('dashboardAvatar').textContent=n.trim().charAt(0).toUpperCase()||"W";localStorage.setItem("worklinkName",n);}}catch(e){console.error(e);}
    try{const r=await fetch(API+"/api/rating?type=worker&phone="+encodeURIComponent(phone),{headers});const d=await r.json();if(d.success)$('myRating').textContent=Number(d.average||0).toFixed(1);}catch(e){}

    async function loadRecommended(){
        const box=$('recommendedBosses'), empty=$('recommendedEmpty'); if(!box)return;
        try{
            const r=await fetch(API+"/api/bosses/search?q=&category=all&availability=available",{headers}); const d=await r.json();
            const bosses=(d.bosses||[]).filter(b=>b.available!==false).slice(0,6); box.innerHTML="";
            if(!bosses.length){empty.style.display="flex";return;} empty.style.display="none";
            bosses.forEach(b=>{const el=document.createElement("article");el.className="recommended-boss-card";const cats=(b.categories||[]).filter(Boolean);const loc=(b.locations||[]).filter(Boolean);el.innerHTML=`<div class="recommended-boss-top"><div class="boss-avatar">${esc((b.name||"B").charAt(0).toUpperCase())}</div><div><span class="online-pill">● Available</span><h3>${esc(b.name||"Employer")}</h3><p>🏢 ${esc(b.company||"Company")}</p></div></div><div class="recommended-meta"><span>📍 ${esc(loc.join(" • ")||"Location not specified")}</span><span>🛠️ ${esc(cats.join(" • ")||"Open work")}</span></div><div class="recommended-actions"><button class="view-boss" type="button">View Details</button><button class="chat-boss" type="button">💬 Chat</button></div>`;el.querySelector(".view-boss").onclick=()=>location.href="boss-profile.html?phone="+encodeURIComponent(b.phone);el.querySelector(".chat-boss").onclick=()=>location.href="chat.html?type=boss&phone="+encodeURIComponent(b.phone)+"&name="+encodeURIComponent(b.name||"Employer");box.appendChild(el);});
        }catch(e){console.error(e);box.innerHTML="";empty.style.display="flex";}
    }
    await loadRecommended();

    async function notifications(){
        const badge=$('notificationBadge'), panel=$('notificationPanel'), list=$('notificationList'); if(!badge||!panel)return;
        const key="worklinkLastNotification_"+role+"_"+phone;
        const lastSeen=Number(localStorage.getItem(key)||0);
        try{
            const r=await fetch(API+"/api/notifications?since=0",{headers});
            const d=await r.json();
            const items=(d.notifications||[]).sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt));
            const unread=items.filter(n=>new Date(n.createdAt).getTime()>lastSeen);
            badge.textContent=unread.length>99?"99+":String(unread.length);
            badge.style.display=unread.length?"flex":"none";
            list.innerHTML=items.length?items.slice(0,10).map(n=>`<div class="notification-item ${new Date(n.createdAt).getTime()>lastSeen?'is-new':''}"><div class="notification-item-icon">💬</div><div><strong>${esc(n.senderName||"New message")}</strong><p>${esc(n.message)}</p><small>${new Date(n.createdAt).toLocaleString([], {dateStyle:"medium",timeStyle:"short"})}</small></div></div>`).join(""):'<div class="notification-empty"><div class="notification-empty-icon">🔔</div><strong>No recent notifications</strong><span>Your new messages will appear here.</span></div>';
        }catch(e){list.innerHTML='<div class="notification-empty"><div class="notification-empty-icon">⚠️</div><strong>Notifications unavailable</strong><span>Please try again in a moment.</span></div>';}
    }
    await notifications(); setInterval(notifications,4000);
    $('notificationBtn')?.addEventListener('click',()=>{const p=$('notificationPanel');p.classList.toggle('open');if(p.classList.contains('open')){localStorage.setItem("worklinkLastNotification_"+role+"_"+phone,String(Date.now()));$('notificationBadge').style.display='none';notifications();}});$('closeNotification')?.addEventListener('click',()=>$('notificationPanel').classList.remove('open'));
    $('logoutBtn')?.addEventListener('click',()=>window.workLinkLogout&&window.workLinkLogout());
});
