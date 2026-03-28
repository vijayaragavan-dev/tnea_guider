const BASE_URL = "http://localhost:8081";

const predictorForm = document.getElementById("predictorForm");
const cutoffInput = document.getElementById("cutoff");
const categoryInput = document.getElementById("category");
const branchInput = document.getElementById("branch");
const budgetInput = document.getElementById("budget");
const districtInput = document.getElementById("district");
const preferenceInput = document.getElementById("preference");

const statusArea = document.getElementById("statusArea");
const resultsSection = document.getElementById("resultsSection");
const compareToolbar = document.getElementById("compareToolbar");
const compareButton = document.getElementById("compareButton");
const chartsSection = document.getElementById("chartsSection");
const comparisonSection = document.getElementById("comparisonSection");
const comparisonBody = document.getElementById("comparisonBody");

const dreamList = document.getElementById("dreamList");
const moderateList = document.getElementById("moderateList");
const safeList = document.getElementById("safeList");

const selectedCollegeIds = new Set();

let placementChart;
let tierChart;

checkBackendConnection();

predictorForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    clearStatus();
    selectedCollegeIds.clear();
    updateCompareButton();

    if (!isCutoffValid(cutoffInput.value)) {
        cutoffInput.classList.add("is-invalid");
        showStatus("Please enter cutoff as integer or .5 only.", "danger");
        return;
    }

    cutoffInput.classList.remove("is-invalid");

    const budgetRaw = String(budgetInput.value || "").trim();
    if (budgetRaw !== "" && Number(budgetRaw) < 50000) {
        showStatus("Budget must be at least 50000 when provided.", "danger");
        return;
    }

    if (!predictorForm.checkValidity()) {
        predictorForm.reportValidity();
        return;
    }

    renderLoading();
    hideResults();
    hideComparison();

    const payload = {
        cutoff: Number(cutoffInput.value),
        category: categoryInput.value,
        branch: normalizeOptional(branchInput.value),
        budget: budgetRaw === "" ? null : Number(budgetRaw),
        district: normalizeOptional(districtInput.value),
        preference: normalizeOptional(preferenceInput.value)
    };

    try {
        const response = await fetch(`${BASE_URL}/api/recommend`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            if (response.status === 404) {
                showStatus("No colleges found. Try increasing budget or removing filters.", "warning");
            } else {
                const message = data.message || "Failed to get recommendations.";
                showStatus(message, "danger");
            }
            return;
        }

        renderResults(data);
        const total = data.dream.length + data.moderate.length + data.safe.length;
        if (total === 0) {
            showStatus("No colleges found. Try increasing budget or removing filters.", "warning");
            return;
        }
        showStatus(`Found ${total} matching colleges.`, "success");
        resultsSection.scrollIntoView({behavior: "smooth", block: "start"});
    } catch (error) {
        showBackendOfflineStatus();
    }
});

resultsSection.addEventListener("change", (event) => {
    if (!event.target.classList.contains("compare-check")) {
        return;
    }

    const id = Number(event.target.dataset.id);
    if (!Number.isFinite(id)) {
        return;
    }

    if (event.target.checked) {
        if (selectedCollegeIds.size >= 3) {
            event.target.checked = false;
            showStatus("You can compare maximum 3 colleges", "warning");
            return;
        }
        selectedCollegeIds.add(id);
    } else {
        selectedCollegeIds.delete(id);
    }
    updateCompareButton();
});

compareButton.addEventListener("click", async () => {
    if (selectedCollegeIds.size < 2) {
        showStatus("Select at least 2 colleges to compare.", "warning");
        return;
    }

    showStatus("Loading comparison...", "info");
    const ids = Array.from(selectedCollegeIds).join(",");

    try {
        const response = await fetch(`${BASE_URL}/api/compare?ids=${encodeURIComponent(ids)}`);
        const data = await response.json();

        if (!response.ok) {
            showStatus(data.message || "Failed to compare colleges.", "danger");
            return;
        }

        renderComparisonTable(data);
        showStatus("Comparison loaded successfully.", "success");
    } catch (error) {
        showBackendOfflineStatus();
    }
});

async function checkBackendConnection() {
    try {
        await fetch(`${BASE_URL}/api/compare?ids=1`);
    } catch (error) {
        showStatus("Backend server is not running on port 8081", "warning");
    }
}

function showBackendOfflineStatus() {
    showStatus("Backend server is not running on port 8081", "danger");
}

function isCutoffValid(rawValue) {
    const value = String(rawValue).trim();
    if (value === "") {
        return false;
    }
    return /^\d+(\.0|\.5)?$/.test(value) && Number(value) >= 0 && Number(value) <= 200;
}

function normalizeOptional(value) {
    const text = String(value || "").trim();
    return text === "" ? null : text;
}

function renderLoading() {
    statusArea.innerHTML = `
        <div class="status-card alert alert-info d-flex align-items-center gap-2" role="alert">
            <div class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></div>
            <span>Analyzing colleges and scoring options...</span>
        </div>
    `;
}

function showStatus(message, type) {
    statusArea.innerHTML = `<div class="status-card alert alert-${type}" role="alert">${message}</div>`;
}

function clearStatus() {
    statusArea.innerHTML = "";
}

function renderResults(data) {
    const allColleges = [...data.dream, ...data.moderate, ...data.safe];
    const bestMatchId = resolveBestMatchId(allColleges);

    renderList(dreamList, data.dream, bestMatchId);
    renderList(moderateList, data.moderate, bestMatchId);
    renderList(safeList, data.safe, bestMatchId);

    resultsSection.classList.remove("d-none");
    compareToolbar.classList.remove("d-none");
    renderCharts(allColleges);
}

function hideResults() {
    resultsSection.classList.add("d-none");
    compareToolbar.classList.add("d-none");
    chartsSection.classList.add("d-none");

    dreamList.innerHTML = "";
    moderateList.innerHTML = "";
    safeList.innerHTML = "";

    destroyCharts();
}

function hideComparison() {
    comparisonSection.classList.add("d-none");
    comparisonBody.innerHTML = "";
}

function renderList(container, colleges, bestMatchId) {
    if (!colleges || colleges.length === 0) {
        container.innerHTML = `<p class="text-secondary mb-0">No colleges in this category.</p>`;
        return;
    }

    container.innerHTML = colleges.map((college, index) => `
        <article class="college-card ${college.collegeId === bestMatchId ? "best-match-card" : ""}" style="animation-delay:${index * 0.05}s">
            ${college.collegeId === bestMatchId ? '<span class="best-badge">Best Match</span>' : ''}
            <div class="college-name">${escapeHtml(college.collegeName)}</div>
            <div class="college-meta">Cutoff: ${formatNumber(college.cutoff)}</div>
            <div class="college-meta">Fees: Rs ${formatNumber(college.fees)}</div>
            <div class="college-meta">Placement: ${formatNumber(college.placementRate)}%</div>
            <div class="college-meta">District: ${escapeHtml(college.district || "N/A")}</div>
            <label class="college-meta mt-2 d-flex align-items-center gap-2">
                <input type="checkbox" class="compare-check" data-id="${college.collegeId}">
                Compare this college
            </label>
        </article>
    `).join("");
}

function resolveBestMatchId(colleges) {
    if (!colleges || colleges.length === 0) {
        return null;
    }

    let best = colleges[0];
    for (const college of colleges) {
        if ((college.finalScore ?? 0) > (best.finalScore ?? 0)) {
            best = college;
        }
    }
    return best.collegeId;
}

function updateCompareButton() {
    compareButton.disabled = selectedCollegeIds.size < 2;
}

function renderComparisonTable(colleges) {
    if (!Array.isArray(colleges) || colleges.length === 0) {
        showStatus("No colleges found for comparison.", "warning");
        return;
    }

    comparisonBody.innerHTML = colleges.map((college) => `
        <tr>
            <td>${escapeHtml(college.collegeName || "N/A")}</td>
            <td>${escapeHtml(college.tier || "N/A")}</td>
            <td>${formatNumber(college.cutoff)}</td>
            <td>Rs ${formatNumber(college.fees)}</td>
            <td>${formatNumber(college.placementRate)}%</td>
            <td>${escapeHtml(college.district || "N/A")}</td>
        </tr>
    `).join("");

    comparisonSection.classList.remove("d-none");
}

function renderCharts(colleges) {
    if (!window.Chart || !Array.isArray(colleges) || colleges.length === 0) {
        return;
    }

    chartsSection.classList.remove("d-none");
    destroyCharts();

    const topByScore = [...colleges]
        .sort((a, b) => (b.finalScore ?? 0) - (a.finalScore ?? 0))
        .slice(0, 8);

    const placementCtx = document.getElementById("placementChart");
    placementChart = new Chart(placementCtx, {
        type: "bar",
        data: {
            labels: topByScore.map((c) => truncate(c.collegeName, 20)),
            datasets: [{
                label: "Placement %",
                data: topByScore.map((c) => Number(c.placementRate) || 0),
                backgroundColor: "rgba(56, 189, 248, 0.7)",
                borderColor: "rgba(56, 189, 248, 1)",
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {labels: {color: "#e2e8f0"}}
            },
            scales: {
                x: {ticks: {color: "#cbd5e1"}},
                y: {ticks: {color: "#cbd5e1"}, beginAtZero: true, max: 100}
            }
        }
    });

    const tierCounts = {"Tier 1": 0, "Tier 2": 0, "Tier 3": 0};
    colleges.forEach((college) => {
        const tier = college.tier || "Tier 3";
        if (tierCounts[tier] !== undefined) {
            tierCounts[tier] += 1;
        }
    });

    const tierCtx = document.getElementById("tierChart");
    tierChart = new Chart(tierCtx, {
        type: "doughnut",
        data: {
            labels: Object.keys(tierCounts),
            datasets: [{
                data: Object.values(tierCounts),
                backgroundColor: ["#34d399", "#38bdf8", "#f97316"]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: {color: "#e2e8f0"}
                }
            }
        }
    });
}

function destroyCharts() {
    if (placementChart) {
        placementChart.destroy();
        placementChart = null;
    }
    if (tierChart) {
        tierChart.destroy();
        tierChart = null;
    }
}

function truncate(value, maxLength) {
    const text = String(value || "");
    if (text.length <= maxLength) {
        return text;
    }
    return `${text.slice(0, maxLength - 1)}…`;
}

function formatNumber(value) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) {
        return "N/A";
    }
    return Number(value).toLocaleString("en-IN", {maximumFractionDigits: 2});
}

function escapeHtml(text) {
    return String(text)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
