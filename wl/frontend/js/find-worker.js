document.addEventListener("DOMContentLoaded", function () {
    const searchInput=document.getElementById("searchInput"), searchBtn=document.getElementById("searchBtn"), jobsContainer=document.getElementById("jobsContainer"), noResults=document.getElementById("noResults"), resultCount=document.getElementById("resultCount");
    let activeFilter="all";
    const API_BASE=window.API_BASE||window.location.origin;

    async function searchWorkers(){
        const q=searchInput.value.trim();
        try{
            const response=await fetch(API_BASE+"/api/workers/search?q="+encodeURIComponent(q)+"&category="+encodeURIComponent(activeFilter));
            const data=await response.json();
            if(!data.success) return showNoResults();
            displayWorkers(Array.isArray(data.workers)?data.workers:[]);
        }catch(err){
            console.error(err); jobsContainer.innerHTML=""; resultCount.textContent="Unable to connect to backend"; noResults.style.display="block";
        }
    }

    function displayWorkers(workers){
        jobsContainer.innerHTML="";
        if(!workers.length){showNoResults();return;}
        noResults.style.display="none"; resultCount.textContent="Showing "+workers.length+" worker(s)";
        workers.forEach(worker=>{
            const card=document.createElement("article"); card.className="job-card";
            card.innerHTML=`<div class="job-main"><div class="job-icon">👷</div><div class="job-info"><h3>${esc(worker.name)}</h3><p class="company">WorkLink Worker</p><p class="location">📍 ${esc(worker.location)}</p><div class="tags"><span>${esc(worker.skill)}</span><span>Available</span></div></div></div><div class="job-side"><strong>👤</strong><button class="details-btn" type="button">View Profile</button><button class="chat-btn" type="button" style="margin-top:8px;">💬 Chat</button></div>`;
            card.querySelector(".details-btn").onclick=()=>location.href="worker-profile.html?phone="+encodeURIComponent(worker.phone); card.querySelector(".chat-btn").onclick=()=>location.href="chat.html?type=worker&phone="+encodeURIComponent(worker.phone)+"&name="+encodeURIComponent(worker.name);
            jobsContainer.appendChild(card);
        });
    }
    function showNoResults(){jobsContainer.innerHTML="";resultCount.textContent="No workers found";noResults.style.display="block";}
    searchBtn.addEventListener("click",searchWorkers);
    searchInput.addEventListener("keydown",e=>{if(e.key==="Enter")searchWorkers();});
    searchInput.addEventListener("input",()=>{if(!searchInput.value.trim())searchWorkers();});
    document.querySelectorAll(".filter").forEach(btn=>btn.addEventListener("click",()=>{
        document.querySelectorAll(".filter").forEach(x=>x.classList.remove("active")); btn.classList.add("active"); activeFilter=btn.dataset.filter||"all"; searchWorkers();
    }));
    function esc(v){return String(v??"").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/\"/g,"&quot;").replace(/'/g,"&#039;");}
    searchWorkers();
});
