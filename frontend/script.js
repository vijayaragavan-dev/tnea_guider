const BASE_URL = "http://localhost:8081/api";

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

async function loadMasterData() {
    try {
        const response = await fetch(`${BASE_URL}/master-data`);
        if (!response.ok) throw new Error("Failed to load master data");
        
        const data = await response.json();
        
        populateDropdown("district", data.districts, "Any District");
        populateDropdown("branch", data.branches, "Choose branch");
        populateDropdown("category", data.categories, "Choose category");
        
        console.log("Master data loaded:", data);
    } catch (error) {
        console.error("Failed to load master data:", error);
    }
}

function populateDropdown(elementId, items, defaultText) {
    const select = document.getElementById(elementId);
    if (!select) return;
    
    select.innerHTML = `<option value="">${defaultText}</option>`;
    
    if (items && Array.isArray(items)) {
        items.forEach(item => {
            const option = document.createElement("option");
            option.value = item;
            option.textContent = item;
            select.appendChild(option);
        });
    }
}

loadMasterData();
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

    const districtValue = districtInput.value ? districtInput.value.trim() : "";
    
    const payload = {
        cutoff: Number(cutoffInput.value),
        category: categoryInput.value,
        branch: branchInput.value || null,
        budget: budgetRaw === "" ? null : Number(budgetRaw),
        district: districtValue === "" ? null : districtValue,
        preference: preferenceInput.value || null
    };

    try {
        const response = await fetch(`${BASE_URL}/recommend`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        
        console.log("API Response:", data);

        if (!response.ok) {
            if (response.status === 404 || (data.dream?.length === 0 && data.moderate?.length === 0 && data.safe?.length === 0)) {
                showEmptyResultsMessage();
                return;
            }
            showUserFriendlyError(data.message);
            return;
        }

        if (!data || (!data.dream && !data.moderate && !data.safe)) {
            showEmptyResultsMessage();
            return;
        }

        renderResults(data);
        const total = (data.dream?.length || 0) + (data.moderate?.length || 0) + (data.safe?.length || 0);
        if (total === 0) {
            showEmptyResultsMessage();
            return;
        }
        showStatus(`Found ${total} matching colleges.`, "success");
        resultsSection.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        console.error("API Error:", error);
        showBackendOfflineStatus();
    }
});

function showEmptyResultsMessage() {
    statusArea.innerHTML = `
        <div class="status-card alert alert-warning" role="alert">
            <strong>⚠ No exact match found.</strong><br>
            Showing closest colleges based on your profile.<br>
            <small>Try adjusting cutoff range, district, or budget filters.</small>
        </div>
    `;
}

function showUserFriendlyError(message) {
    showStatus("Something went wrong. Please try again.", "danger");
}

resultsSection.addEventListener("change", (event) => {
    if (!event.target.classList.contains("compare-check")) {
        return;
    }

    const id = Number(event.target.dataset.id);
    if (!Number.isFinite(id)) {
        return;
    }

    if (event.target.checked) {
        selectedCollegeIds.add(id);
    } else {
        selectedCollegeIds.delete(id);
    }
    updateCompareButton();
});

compareButton.addEventListener("click", function() {
    console.log("Compare button clicked");
    console.log("Selected IDs:", Array.from(selectedCollegeIds));
    console.log("Current colleges:", currentColleges);
    
    if (selectedCollegeIds.size < 2) {
        console.log("Less than 2 selected");
        showStatus("Select at least 2 colleges to compare.", "warning");
        alert("Please select at least 2 colleges to compare");
        return;
    }

    const selectedColleges = currentColleges.filter(c => selectedCollegeIds.has(c.collegeId));
    console.log("Filtered colleges:", selectedColleges);
    
    if (selectedColleges.length < 2) {
        showStatus("Select at least 2 colleges to compare.", "warning");
        alert("Please select at least 2 colleges to compare");
        return;
    }

    console.log("Opening comparison modal with:", selectedColleges.length, "colleges");
    openComparisonModal(selectedColleges);
});

async function checkBackendConnection() {
    try {
        await fetch(`${BASE_URL}/compare?ids=1`);
    } catch (error) {
        showStatus("Unable to connect to server. Please ensure the backend is running.", "warning");
    }
}

function showBackendOfflineStatus() {
    showStatus("Something went wrong. Please try again.", "danger");
}

function isCutoffValid(rawValue) {
    const value = String(rawValue).trim();
    if (value === "") {
        return false;
    }
    return /^\d+(\.0|\.5)?$/.test(value) && Number(value) >= 0 && Number(value) <= 200;
}

function renderLoading() {
    statusArea.innerHTML = `
        <div class="status-card alert alert-info d-flex align-items-center gap-2" role="alert">
            <div class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></div>
            <span>🔍 Analyzing best colleges for you...</span>
        </div>
    `;
}

function showStatus(message, type) {
    statusArea.innerHTML = `<div class="status-card alert alert-${type}" role="alert">${message}</div>`;
}

function clearStatus() {
    statusArea.innerHTML = "";
}

let currentColleges = [];

function renderResults(data) {
    const dream = data.dream || [];
    const moderate = data.moderate || [];
    const safe = data.safe || [];
    
    currentColleges = [...dream, ...moderate, ...safe];
    const bestMatchId = resolveBestMatchId(currentColleges);

    renderList(dreamList, dream, bestMatchId, "dream");
    renderList(moderateList, moderate, bestMatchId, "moderate");
    renderList(safeList, safe, bestMatchId, "safe");

    resultsSection.classList.remove("d-none");
    compareToolbar.classList.remove("d-none");
    renderCharts(currentColleges);
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

function renderList(container, colleges, bestMatchId, categoryType) {
    if (!colleges || colleges.length === 0) {
        container.innerHTML = `<p class="text-secondary mb-0">No colleges in this category.</p>`;
        return;
    }

    const seen = new Set();
    const uniqueColleges = [];
    for (const college of colleges) {
        if (!seen.has(college.collegeId)) {
            seen.add(college.collegeId);
            uniqueColleges.push(college);
        }
    }

    const limitedColleges = uniqueColleges.slice(0, 10);
    
    container.innerHTML = limitedColleges.map((college, index) => `
        <article class="college-card ${college.collegeId === bestMatchId ? "best-match-card" : ""}" style="animation-delay:${index * 0.05}s">
            ${college.collegeId === bestMatchId ? '<span class="best-badge">Best Match</span>' : ''}
            <span class="category-tag category-${categoryType}">${categoryType}</span>
            <div class="college-name">${escapeHtml(college.collegeName)}</div>
            <div class="college-meta">Cutoff: ${formatNumber(college.cutoff)}</div>
            <div class="college-meta">Fees: Rs ${formatNumber(college.fees)}</div>
            <div class="college-meta">Placement: ${formatNumber(college.placementRate)}%</div>
            <div class="college-meta">District: ${escapeHtml(college.district || "Not specified")}</div>
            <div class="college-actions mt-2">
                <button class="btn btn-sm btn-action" onclick="showReview('${escapeHtml(college.collegeName)}', ${college.cutoff}, ${college.placementRate}, ${college.fees}, '${escapeHtml(college.district || '')}')">View Review</button>
                <button class="btn btn-sm btn-action" onclick="showLocation('${escapeHtml(college.collegeName)}', '${escapeHtml(college.district || '')}')">📍 View Location</button>
                <button class="btn btn-sm btn-action-why" onclick="showWhy('${escapeHtml(college.collegeName)}')">Why this college?</button>
            </div>
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
            <td>${escapeHtml(college.collegeName || "Unknown College")}</td>
            <td>${escapeHtml(college.tier || "Standard")}</td>
            <td>${formatNumber(college.cutoff)}</td>
            <td>Rs ${formatNumber(college.fees)}</td>
            <td>${formatNumber(college.placementRate)}%</td>
            <td>${escapeHtml(college.district || "Not specified")}</td>
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
        return "Not specified";
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

function showReview(collegeName, cutoff, placement, fees, district) {
    console.log("showReview called with:", collegeName, district);
    openModal("Student Reviews", '<div class="text-center"><div class="spinner-border text-primary" role="status"></div><p class="mt-2">Loading student reviews...</p></div>');
    
    let url = `${BASE_URL}/review?collegeName=${encodeURIComponent(collegeName)}`;
    if (district && district.trim()) url += `&district=${encodeURIComponent(district.trim())}`;
    console.log("Fetching URL:", url);
    
    fetch(url)
        .then(response => {
            console.log("Response status:", response.status);
            return response.json();
        })
        .then(data => {
            console.log("API Response data:", JSON.stringify(data));
            const reviews = data.reviews || [];
            console.log("Reviews array:", reviews.length);
            
            if (reviews.length === 0) {
                document.getElementById("modalContent").innerHTML = '<p class="text-warning">No reviews available. Please try again later.</p>';
                return;
            }
            
            const totalReviews = reviews.length;
            const displayReviews = reviews.slice(0, 2);
            const hasMore = totalReviews > 2;
            
            let reviewsHtml = displayReviews.map(r => {
                const stars = '⭐'.repeat(parseInt(r.rating) || 3);
                return `<div class="review-card">
                    <div class="review-header">
                        <span class="review-stars">${stars}</span>
                        <span class="review-name">${escapeHtml(r.name)}</span>
                    </div>
                    <div class="review-text">"${escapeHtml(r.review)}"</div>
                </div>`;
            }).join('');
            
            let readMoreBtn = '';
            if (hasMore) {
                const remainingReviews = reviews.slice(2);
                readMoreBtn = `<div class="more-reviews" id="moreReviews" style="display:none;">
                    ${remainingReviews.map(r => {
                        const stars = '⭐'.repeat(parseInt(r.rating) || 3);
                        return `<div class="review-card">
                            <div class="review-header">
                                <span class="review-stars">${stars}</span>
                                <span class="review-name">${escapeHtml(r.name)}</span>
                            </div>
                            <div class="review-text">"${escapeHtml(r.review)}"</div>
                        </div>`;
                    }).join('')}
                </div>
                <button class="read-more-btn" onclick="toggleMoreReviews()">Read More Reviews (${totalReviews - 2} more)</button>`;
            }
            
            const content = `<div class="reviews-container">
                <div class="reviews-header">
                    <span class="reviews-icon">💬</span>
                    <h4>${escapeHtml(data.college)}</h4>
                    <span class="reviews-count">${totalReviews} reviews</span>
                </div>
                <div class="reviews-list">${reviewsHtml}${readMoreBtn}</div>
            </div>`;
            document.getElementById("modalContent").innerHTML = content;
        })
        .catch(error => {
            console.error("Review error:", error);
            document.getElementById("modalContent").innerHTML = '<p class="text-danger">Failed to load reviews. Please try again.</p>';
        });
}

function toggleMoreReviews() {
    const moreSection = document.getElementById("moreReviews");
    const btn = document.querySelector(".read-more-btn");
    if (moreSection.style.display === "none") {
        moreSection.style.display = "block";
        btn.textContent = "Show Less";
    } else {
        moreSection.style.display = "none";
        const total = document.querySelectorAll(".review-card").length;
        btn.textContent = `Read More Reviews (${total - 2} more)`;
    }
}

function showWhy(collegeName) {
    const cutoff = cutoffInput.value || "";
    const category = categoryInput.value || "";
    
    openModal("Why This College?", '<div class="text-center"><div class="spinner-border text-primary" role="status"></div><p class="mt-2">Analyzing suitability...</p></div>');
    
    fetch(`${BASE_URL}/why?collegeName=${encodeURIComponent(collegeName)}&cutoff=${encodeURIComponent(cutoff)}&category=${encodeURIComponent(category)}`)
        .then(response => response.json())
        .then(data => {
            const content = `<div class="review-box ai-review">
                <div class="ai-review-header">
                    <span class="ai-icon">⭐</span>
                    <h4>Why This College?</h4>
                </div>
                <div class="ai-review-college">${escapeHtml(data.college)}</div>
                <div class="ai-review-text">${escapeHtml(data.reason).replace(/\n/g, '<br>')}</div>
            </div>`;
            document.getElementById("modalContent").innerHTML = content;
        })
        .catch(error => {
            console.error("Why error:", error);
            document.getElementById("modalContent").innerHTML = '<p class="text-danger">Unable to load explanation. Please try again.</p>';
        });
}

function showLocation(collegeName, district) {
    try {
        let query;
        if (district && district !== "Not specified") {
            query = collegeName + " " + district + " Tamil Nadu";
        } else {
            query = collegeName + " Tamil Nadu";
        }
        const url = "https://www.google.com/maps/search/" + encodeURIComponent(query);
        window.open(url, "_blank");
    } catch (error) {
        console.error("Location Error:", error);
        alert("Unable to open location. Please try again.");
    }
}

function openComparisonModal(colleges) {
    console.log("openComparisonModal called with:", colleges);
    
    if (!colleges || colleges.length < 2) {
        console.log("Validation failed: less than 2 colleges");
        showStatus("Select at least 2 colleges to compare.", "warning");
        return;
    }

    console.log("Building comparison for", colleges.length, "colleges");

    const bestPlacement = Math.max(...colleges.map(c => Number(c.placementRate) || 0));
    const lowestFees = Math.min(...colleges.map(c => Number(c.fees) || Infinity));
    const lowestCutoff = Math.min(...colleges.map(c => Number(c.cutoff) || Infinity));

    console.log("Best values - Placement:", bestPlacement, "Fees:", lowestFees, "Cutoff:", lowestCutoff);

    const badgeCell = (value, isBest, type) => {
        if (!isBest) return escapeHtml(value);
        const badgeClass = type === 'placement' ? 'badge-best-placement' : type === 'fees' ? 'badge-best-fees' : 'badge-best-cutoff';
        return `<span class="comparison-badge ${badgeClass}">${escapeHtml(value)}</span>`;
    };

    const thead = `<thead><tr><th class="attr-col sticky-col">Attribute</th>${colleges.map((c, i) => `<th class="college-header">${escapeHtml(c.collegeName)}</th>`).join('')}</tr></thead>`;
    
    const rows = [
        { label: 'District', key: 'district', type: 'text' },
        { label: 'Cutoff', key: 'cutoff', type: 'number', isBest: true, bestFor: 'lowest' },
        { label: 'Fees (INR)', key: 'fees', type: 'number', isBest: true, bestFor: 'lowest' },
        { label: 'Placement %', key: 'placementRate', type: 'number', isBest: true, bestFor: 'highest' },
        { label: 'Tier', key: 'tier', type: 'text' }
    ];

    const tbody = `<tbody>${rows.map(row => {
        let bestValue = row.isBest ? (row.bestFor === 'highest' ? bestPlacement : lowestCutoff || lowestFees) : null;
        return `<tr>
            <td class="attr-label sticky-col">${row.label}</td>
            ${colleges.map(c => {
                const value = c[row.key];
                const numValue = Number(value) || 0;
                const isBest = row.isBest && numValue === bestValue;
                if (row.type === 'number') {
                    return `<td>${badgeCell(row.key === 'fees' ? 'Rs ' + formatNumber(value) : formatNumber(value), isBest, row.key === 'placementRate' ? 'placement' : row.key === 'fees' ? 'fees' : 'cutoff')}</td>`;
                }
                return `<td>${escapeHtml(value || 'Not specified')}</td>`;
            }).join('')}
        </tr>`;
    }).join('')}</tbody>`;

    const tableHtml = `<div class="comparison-table-wrapper"><table class="comparison-table">${thead}${tbody}</table></div>`;
    
    const summaryHtml = `<div class="comparison-summary">
        <h4>Quick Summary</h4>
        <div class="summary-grid">
            <div class="summary-item">
                <span class="summary-label">Highest Placement</span>
                <span class="summary-value badge-best-placement">${colleges.find(c => Number(c.placementRate) === bestPlacement)?.collegeName || 'N/A'}</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Lowest Fees</span>
                <span class="summary-value badge-best-fees">${colleges.find(c => Number(c.fees) === lowestFees)?.collegeName || 'N/A'}</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Best Cutoff</span>
                <span class="summary-value badge-best-cutoff">${colleges.find(c => Number(c.cutoff) === lowestCutoff)?.collegeName || 'N/A'}</span>
            </div>
        </div>
    </div>`;

    const content = `<div class="comparison-modal">
        <p class="comparison-info">Comparing <strong>${colleges.length}</strong> colleges</p>
        ${tableHtml}
        ${summaryHtml}
    </div>`;

    console.log("Calling openModal...");
    openModal("College Comparison", content, true);
}

function openModal(title, content, isLarge = false) {
    console.log("openModal called with title:", title);
    console.log("Modal elements - modalTitle:", !!document.getElementById("modalTitle"), "modalContent:", !!document.getElementById("modalContent"), "modalOverlay:", !!document.getElementById("modalOverlay"));
    
    document.getElementById("modalTitle").textContent = title;
    document.getElementById("modalContent").innerHTML = content;
    if (isLarge) {
        document.querySelector('.modal-container').classList.add('modal-large');
    } else {
        document.querySelector('.modal-container').classList.remove('modal-large');
    }
    document.getElementById("modalOverlay").classList.remove("d-none");
    document.body.style.overflow = "hidden";
    console.log("Modal should now be visible");
}

function closeModal() {
    document.getElementById("modalOverlay").classList.add("d-none");
    document.body.style.overflow = "";
    document.getElementById("modalContent").innerHTML = "";
}

function closeModalOnOverlay(event) {
    if (event.target === document.getElementById("modalOverlay")) {
        closeModal();
    }
}

document.addEventListener("keydown", function(event) {
    if (event.key === "Escape") {
        closeModal();
    }
});