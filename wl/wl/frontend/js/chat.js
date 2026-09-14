document.addEventListener("DOMContentLoaded",()=>{
 const role=localStorage.getItem("worklinkRole"), phone=localStorage.getItem("worklinkPhone");
 const p=new URLSearchParams(location.search), peerType=p.get("type"), peerPhone=p.get("phone"), givenName=p.get("name");
 const nameEl=document.getElementById("peerName"), box=document.getElementById("messages"), status=document.getElementById("status"), form=document.getElementById("chatForm"), input=document.getElementById("messageInput"), send=document.getElementById("sendBtn");
 if(localStorage.getItem("worklinkLoggedIn")!=="true"||!role||!phone||!peerType||!peerPhone){location.href="login.html";return;}
 if(peerType===role&&peerPhone===phone){location.href=role==="worker"?"worker-dashboard.html":"boss-dashboard.html";return;}
 nameEl.textContent=givenName||"WorkLink User";
 document.getElementById("backBtn").onclick=()=>history.length>1?history.back():(location.href=role==="worker"?"worker-dashboard.html":"boss-dashboard.html");
 function esc(v){return String(v??"").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#039;");}
 function time(v){try{return new Date(v).toLocaleString([], {hour:"2-digit",minute:"2-digit"});}catch(_){return "";}}
 async function load(){try{const r=await fetch((window.API_BASE||location.origin)+"/api/chat?peerType="+encodeURIComponent(peerType)+"&peerPhone="+encodeURIComponent(peerPhone));const d=await r.json();if(!d.success)throw Error(d.message||"Unable to load chat");if(d.peerName)nameEl.textContent=d.peerName;box.innerHTML="";(d.messages||[]).forEach(m=>{const el=document.createElement("div");el.className="msg "+(m.senderType===role&&m.senderPhone===phone?"mine":"theirs");el.innerHTML=esc(m.message)+`<div class="time">${esc(time(m.createdAt))}</div>`;box.appendChild(el);});box.scrollTop=box.scrollHeight;status.textContent=(d.messages||[]).length?"":"No messages yet — start the conversation.";}catch(e){console.error(e);status.textContent=e.message||"Unable to load chat";}}
 form.addEventListener("submit",async e=>{e.preventDefault();const message=input.value.trim();if(!message)return;send.disabled=true;try{const r=await fetch((window.API_BASE||location.origin)+"/api/chat",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({receiverType:peerType,receiverPhone:peerPhone,message})});const d=await r.json();if(!d.success)throw Error(d.message||"Could not send message");input.value="";await load();}catch(e){alert(e.message||"Could not send message");}finally{send.disabled=false;input.focus();}});
 load();setInterval(load,3000);
});
