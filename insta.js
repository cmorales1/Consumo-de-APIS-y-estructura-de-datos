const API_KEY = "fb2a5107a6msh9b6eb177c4aa73bp13c14cjsndc986bc34403"; 
const API_HOST = "instagram120.p.rapidapi.com";
const API_URL = "https://instagram120.p.rapidapi.com/api/instagram/d_cas77";

async function fetchInstagramProfile(username) {
    const url = `${API_URL}?user=${username}`;

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "X-RapidAPI-Key": API_KEY,
                "X-RapidAPI-Host": API_HOST
            }
        });

        const text = await response.text();
        console.log("Respuesta raw:", text);

        if (!response.ok) {
            throw new Error(`Error en la petición: ${response.status}`);
        }

        const data = JSON.parse(text);
        return data;
    } catch (error) {
        console.error("Fetch error:", error);
        return null;
    }
}


function createCard(profile) {
 

    const card = document.createElement("div");
    card.className = "card card-custom mb-4";

    card.innerHTML = `
        <div class="row g-0 align-items-center">
            <div class="col-md-4 text-center p-3">
                <img src="${profile.picture}" class="img-fluid rounded-circle" alt="${profile.username}" style="max-width: 120px;" />
            </div>
            <div class="col-md-8">
                <div class="card-body">
                    <h5 class="card-title">${profile.username}</h5>
                    <p class="card-text">${profile.full_name || "Sin nombre completo"}</p>
                    <p class="card-text"><small class="text-muted">${profile.biography || "Sin biografía"}</small></p>
                    <p class="card-text">Seguidores: ${profile.followers || "N/A"}</p>
                    <p class="card-text">Seguidos: ${profile.following || "N/A"}</p>
                </div>
            </div>
        </div>
    `;

    return card;
}

async function main() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = "<p>Cargando perfiles...</p>";

    
    const usuarios = ["instagram"];

    mainContent.innerHTML = "";  

    for (const user of usuarios) {
        const profileData = await fetchInstagramProfile(user);
        if (profileData && profileData.data) {
            const card = createCard(profileData.data);
            mainContent.appendChild(card);
        } else {
            const errorDiv = document.createElement("div");
            errorDiv.className = "alert alert-danger";
            errorDiv.textContent = `No se pudo cargar el perfil de ${user}`;
            mainContent.appendChild(errorDiv);
        }
    }
}

main();
