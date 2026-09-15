document.addEventListener("DOMContentLoaded",()=>{
 const role=localStorage.getItem("worklinkRole"), phone=localStorage.getItem("worklinkPhone");
 if(localStorage.getItem("worklinkLoggedIn")!=="true"||role!=="boss"||!phone){location.href="login.html";return;}
 const form=document.getElementById("workForm"),msg=document.getElementById("message"),btn=document.getElementById("postBtn");
 form.addEventListener("submit",async e=>{e.preventDefault();btn.disabled=true;btn.textContent="Posting...";msg.textContent="";
  const body={bossPhone:phone,title:document.getElementById("title").value.trim(),category:document.getElementById("category").value,location:document.getElementById("location").value.trim(),salary:document.getElementById("salary").value,type:document.getElementById("type").value,description:document.getElementById("description").value.trim()};
  try{const r=await fetch((window.API_BASE||location.origin)+"/api/jobs",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(body)});const d=await r.json();if(d.success){msg.style.color="green";msg.textContent="Work posted successfully! Workers can now see it.";form.reset();}else{msg.style.color="red";msg.textContent=d.message||"Could not post work.";}}catch(err){msg.style.color="red";msg.textContent="Server error. Please try again.";}finally{btn.disabled=false;btn.textContent="Post Work";}
 });
});
