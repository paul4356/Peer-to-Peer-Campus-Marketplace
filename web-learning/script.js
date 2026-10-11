/**
 * ==============================================================================
 * PEER-TO-PEER CAMPUS MARKETPLACE - CLIENT-SIDE JAVASCRIPT LAB
 * Demonstrates:
 * 1. Variables (let, const), Operators (arithmetic, logical, ternary)
 * 2. Conditionals (if/else, switch) and Loops (for, forEach, map, filter, reduce)
 * 3. Functions (arrow functions, pure functions, event callbacks)
 * 4. DOM Manipulation (createElement, querySelector, appendChild, classList)
 * 5. Event Listeners (click, input, submit, change)
 * 6. Form Validation with custom regex and real-time feedback
 * ==============================================================================
 */

// Active State Variables
let currentProducts = [];
let marketplaceCategories = [];
let authToken = sessionStorage.getItem('marketplaceToken');
let selectedCategory = 'ALL';
let searchQuery = '';
let currentSort = 'featured';

async function apiRequest(path, options = {}) {
  const headers = { Accept: 'application/json', ...(options.headers || {}) };
  if (options.body) headers['Content-Type'] = 'application/json';
  if (authToken) headers.Authorization = `Bearer ${authToken}`;

  const response = await fetch(path, { ...options, headers });
  const payload = await response.json().catch(() => null);
  if (!response.ok || !payload || payload.success !== true) {
    const error = new Error(payload?.error || payload?.message || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }
  return payload.data;
}

function setAuthMessage(message, isSuccess = false) {
  const messageEl = document.getElementById('authMessage');
  messageEl.textContent = message;
  messageEl.classList.toggle('success', isSuccess);
}

function showAuthenticatedApp(user) {
  document.getElementById('authPage').classList.add('hidden');
  document.getElementById('appContainer').classList.remove('hidden');
  document.getElementById('accountControls').classList.remove('hidden');
  document.getElementById('accountName').textContent = `${user.full_name} · ${user.verification_status}`;
  loadMarketplace().catch(error => {
    document.getElementById('itemCountLabel').textContent = `Could not load marketplace: ${error.message}`;
  });
}

function showAuthentication() {
  document.getElementById('authPage').classList.remove('hidden');
  document.getElementById('appContainer').classList.add('hidden');
  document.getElementById('accountControls').classList.add('hidden');
  registerForm.classList.add('hidden');
  loginForm.classList.remove('hidden');
  document.getElementById('authTitle').textContent = 'Welcome back';
  document.getElementById('authSubtitle').textContent = 'Sign in to browse and trade academic essentials with your campus community.';
}

async function loadMarketplace() {
  const [categoryData, listingData] = await Promise.all([
    apiRequest('/api/categories'),
    apiRequest('/api/listings?limit=50&sort=newest')
  ]);
  marketplaceCategories = categoryData.categories || [];
  currentProducts = (listingData.listings || []).map(listing => ({
    id: listing.id,
    title: listing.title,
    category: listing.category.name,
    categoryId: listing.category.id,
    price: listing.price,
    condition: listing.condition_state.toUpperCase(),
    seller: listing.seller.name,
    sellerEmail: listing.seller.email,
    description: listing.description,
    image: /^https?:\/\//i.test(listing.image_url || '')
      ? listing.image_url
      : 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'
  }));
  renderCategories();
  renderProducts();
}

function renderCategories() {
  categoryFilterList.innerHTML = '<li><button class="filter-btn active" data-category="ALL">ALL</button></li>';
  marketplaceCategories.forEach(category => {
    const item = document.createElement('li');
    const button = document.createElement('button');
    button.className = 'filter-btn';
    button.dataset.category = String(category.id);
    button.textContent = category.name;
    item.appendChild(button);
    categoryFilterList.appendChild(item);
  });

  const categorySelect = document.getElementById('itemCategory');
  categorySelect.innerHTML = '<option value="">-- Select Category --</option>';
  marketplaceCategories.forEach(category => {
    const option = document.createElement('option');
    option.value = String(category.id);
    option.textContent = category.name;
    categorySelect.appendChild(option);
  });
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, character => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[character]);
}

// DOM Element Selectors
const productsGrid = document.getElementById('productsGrid');
const emptyState = document.getElementById('emptyState');
const itemCountLabel = document.getElementById('itemCountLabel');
const searchInput = document.getElementById('searchInput');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const sortSelect = document.getElementById('sortSelect');
const categoryFilterList = document.getElementById('categoryFilterList');
const resetFiltersBtn = document.getElementById('resetFiltersBtn');

// Navigation Tabs
const gridTabBtn = document.getElementById('gridTabBtn');
const formTabBtn = document.getElementById('formTabBtn');
const labTabBtn = document.getElementById('labTabBtn');
const gridSection = document.getElementById('gridSection');
const formSection = document.getElementById('formSection');
const labSection = document.getElementById('labSection');

// Modal Elements
const productModal = document.getElementById('productModal');
const modalCloseBtn = document.getElementById('modalCloseBtn');
const modalBody = document.getElementById('modalBody');

// ------------------------------------------------------------------------------
// 1. Functions & DOM Manipulation: Render Products
// ------------------------------------------------------------------------------

/**
 * Renders the products array dynamically into the DOM grid.
 * Demonstrates: createElement, classList, innerHTML, appendChild
 */
function renderProducts() {
  productsGrid.innerHTML = '';

  // Filter products by Category and Search Term
  const filtered = currentProducts.filter(item => {
    const matchesCategory = selectedCategory === 'ALL' || String(item.categoryId) === selectedCategory;

    const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.seller.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesCategory && matchesSearch;
  });

  // Sort products
  filtered.sort((a, b) => {
    if (currentSort === 'price-asc') return a.price - b.price;
    if (currentSort === 'price-desc') return b.price - a.price;
    if (currentSort === 'name-asc') return a.title.localeCompare(b.title);
    return b.id - a.id; // newest listings first
  });

  // Update item count label
  itemCountLabel.textContent = `Showing ${filtered.length} academic items`;

  // Empty state handling
  if (filtered.length === 0) {
    emptyState.classList.remove('hidden');
    productsGrid.classList.add('hidden');
    return;
  } else {
    emptyState.classList.add('hidden');
    productsGrid.classList.remove('hidden');
  }

  // Generate cards
  filtered.forEach(product => {
    const card = document.createElement('article');
    card.className = 'product-card';
    card.setAttribute('data-id', product.id);

    const conditionClass = product.condition.toLowerCase().replace(/\s+/g, '-');
    const title = escapeHtml(product.title);
    const category = escapeHtml(product.category);
    const seller = escapeHtml(product.seller);
    const description = escapeHtml(product.description);
    const image = escapeHtml(product.image);

    card.innerHTML = `
      <div class="card-image-wrap">
        <img src="${image}" alt="${title}" class="card-image" loading="lazy">
        <span class="condition-badge ${escapeHtml(conditionClass)}">${escapeHtml(product.condition)}</span>
      </div>
      <div class="card-content">
        <span class="card-category">${category}</span>
        <div class="card-header-row">
          <h3 class="card-title">${title}</h3>
          <span class="card-price">₹${escapeHtml(product.price)}</span>
        </div>
        <div class="card-seller">BY ${seller}</div>
        <div class="card-actions">
          <button class="btn-chat" data-action="chat" data-id="${product.id}">
            ✉️ CONTACT
          </button>
          <button class="btn-info" data-action="info" data-id="${product.id}" title="Item Details">
            ℹ️
          </button>
        </div>
      </div>
    `;

    productsGrid.appendChild(card);
  });
}

// ------------------------------------------------------------------------------
// 2. Event Listeners: Search, Filters & Modals
// ------------------------------------------------------------------------------

// Real-time Search with Input Event
searchInput.addEventListener('input', (e) => {
  searchQuery = e.target.value.trim();
  renderProducts();
});

// Clear Search button
clearSearchBtn.addEventListener('click', () => {
  searchInput.value = '';
  searchQuery = '';
  renderProducts();
});

// Category Filter Buttons
categoryFilterList.addEventListener('click', (e) => {
  if (e.target.tagName === 'BUTTON') {
    // Remove active from all buttons
    categoryFilterList.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    e.target.classList.add('active');
    selectedCategory = e.target.getAttribute('data-category');
    renderProducts();
  }
});

// Sort Select Change
sortSelect.addEventListener('change', (e) => {
  currentSort = e.target.value;
  renderProducts();
});

// Reset Filters
resetFiltersBtn.addEventListener('click', () => {
  selectedCategory = 'ALL';
  searchQuery = '';
  searchInput.value = '';
  categoryFilterList.querySelectorAll('.filter-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-category') === 'ALL');
  });
  renderProducts();
});

// Card Action Delegation (Chat & Info Modals)
productsGrid.addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;

  const action = btn.getAttribute('data-action');
  const id = parseInt(btn.getAttribute('data-id'), 10);
  const item = currentProducts.find(p => p.id === id);

  if (!item) return;

  if (action === 'chat') {
    openModal(`
      <h2 style="color:#fff; margin-bottom:0.5rem;">Contact ${escapeHtml(item.seller)}</h2>
      <p style="color:#a1a1aa; font-size:0.85rem; margin-bottom:1rem;">Item: <strong>${escapeHtml(item.title)}</strong> (₹${escapeHtml(item.price)})</p>
      <div style="background:#121214; padding:0.85rem; border-radius:6px; margin-bottom:1rem; font-size:0.85rem; color:#f4f4f5;">
        <em>Seller email: ${escapeHtml(item.sellerEmail)}</em>
      </div>
      <a class="primary-btn" href="mailto:${encodeURIComponent(item.sellerEmail)}?subject=${encodeURIComponent(`Campus listing: ${item.title}`)}">Email seller</a>
    `);
  } else if (action === 'info') {
    openModal(`
      <h2 style="color:#fff; margin-bottom:0.4rem;">${escapeHtml(item.title)}</h2>
      <div style="display:flex; justify-content:space-between; margin-bottom:1rem;">
        <span style="color:#f26440; font-weight:700;">${escapeHtml(item.category)}</span>
        <span style="color:#fff; font-size:1.2rem; font-weight:800;">₹${escapeHtml(item.price)}</span>
      </div>
      <p style="color:#a1a1aa; line-height:1.6; margin-bottom:1rem;">${escapeHtml(item.description)}</p>
      <div style="background:#202024; padding:0.75rem; border-radius:6px; font-size:0.8rem; color:#f4f4f5;">
        <div><strong>Condition:</strong> ${escapeHtml(item.condition)}</div>
        <div><strong>Student Seller:</strong> ${escapeHtml(item.seller)}</div>
      </div>
    `);
  }
});

function openModal(contentHtml) {
  modalBody.innerHTML = contentHtml;
  productModal.classList.remove('hidden');
}

modalCloseBtn.addEventListener('click', () => {
  productModal.classList.add('hidden');
});

// Close modal when clicking outside content
window.addEventListener('click', (e) => {
  if (e.target === productModal) {
    productModal.classList.add('hidden');
  }
});

// ------------------------------------------------------------------------------
// 3. Tab Navigation (Switching Views)
// ------------------------------------------------------------------------------
function switchTab(activeBtn, activeSection) {
  [gridTabBtn, formTabBtn, labTabBtn].forEach(btn => btn.classList.remove('active'));
  [gridSection, formSection, labSection].forEach(sec => sec.classList.add('hidden'));

  activeBtn.classList.add('active');
  activeSection.classList.remove('hidden');
}

gridTabBtn.addEventListener('click', () => switchTab(gridTabBtn, gridSection));
formTabBtn.addEventListener('click', () => switchTab(formTabBtn, formSection));
labTabBtn.addEventListener('click', () => switchTab(labTabBtn, labSection));

// Promo Exam Hub Button
document.getElementById('openExamHubBtn').addEventListener('click', () => {
  alert('Exam Hub: Semester question papers, graphics drawing templates, and notes repository active!');
});

// ------------------------------------------------------------------------------
// 4. Client-Side Form Validation & DOM Injection
// ------------------------------------------------------------------------------
const listingForm = document.getElementById('listingForm');

listingForm.addEventListener('submit', async (e) => {
  e.preventDefault();

  // Reset errors
  document.querySelectorAll('.error-msg').forEach(el => el.textContent = '');
  document.querySelectorAll('.form-group input, .form-group select, .form-group textarea').forEach(el => el.classList.remove('invalid'));

  let isValid = true;

  // Title validation
  const titleInput = document.getElementById('itemTitle');
  if (titleInput.value.trim().length < 4) {
    document.getElementById('itemTitleError').textContent = 'Item title must be at least 4 characters long.';
    titleInput.classList.add('invalid');
    isValid = false;
  }

  // Category validation
  const categoryInput = document.getElementById('itemCategory');
  if (!categoryInput.value) {
    document.getElementById('itemCategoryError').textContent = 'Please choose an academic category.';
    categoryInput.classList.add('invalid');
    isValid = false;
  }

  // Price validation
  const priceInput = document.getElementById('itemPrice');
  const priceVal = parseFloat(priceInput.value);
  if (isNaN(priceVal) || priceVal <= 0) {
    document.getElementById('itemPriceError').textContent = 'Please enter a valid price greater than ₹0.';
    priceInput.classList.add('invalid');
    isValid = false;
  }

  // Description validation
  const descInput = document.getElementById('itemDescription');
  if (descInput.value.trim().length < 10) {
    document.getElementById('itemDescriptionError').textContent = 'Description must be at least 10 characters long.';
    descInput.classList.add('invalid');
    isValid = false;
  }

  // Terms Checkbox
  const termsInput = document.getElementById('agreeTerms');
  if (!termsInput.checked) {
    document.getElementById('agreeTermsError').textContent = 'You must confirm campus honor code compliance.';
    isValid = false;
  }

  if (!isValid) return;

  // Selected Condition from Radio buttons
  const selectedConditionRadio = document.querySelector('input[name="itemCondition"]:checked');
  const condition = selectedConditionRadio ? selectedConditionRadio.value : 'GOOD';

  const alertBox = document.getElementById('formSuccessAlert');
  const submitButton = document.getElementById('submitItemBtn');
  const conditionMap = {
    'NEW': 'New',
    'LIKE NEW': 'Like New',
    'GOOD': 'Good',
    'ACCEPTABLE': 'Acceptable'
  };
  submitButton.disabled = true;
  alertBox.classList.add('hidden');
  try {
    await apiRequest('/api/listings', {
      method: 'POST',
      body: JSON.stringify({
        title: titleInput.value.trim(),
        description: descInput.value.trim(),
        price: priceVal,
        category_id: Number(categoryInput.value),
        condition_state: conditionMap[condition]
      })
    });
    alertBox.textContent = 'Your listing was published to the marketplace.';
    alertBox.classList.remove('hidden');
    alertBox.classList.add('success');
    listingForm.reset();
    switchTab(gridTabBtn, gridSection);
    try {
      await loadMarketplace();
    } catch (error) {
      alertBox.textContent = `Listing published, but the marketplace could not refresh: ${error.message}`;
    }
  } catch (error) {
    alertBox.textContent = error.message;
    alertBox.classList.remove('success');
    alertBox.classList.remove('hidden');
  } finally {
    submitButton.disabled = false;
  }
});

// ------------------------------------------------------------------------------
// 5. JavaScript Syllabus Lab Interactive Demonstrations
// ------------------------------------------------------------------------------

// Demo 1: Variables & Operators
document.getElementById('runCalcBtn').addEventListener('click', () => {
  const original = parseFloat(document.getElementById('labOriginalPrice').value) || 0;
  const discountPercent = parseFloat(document.getElementById('labDiscount').value) || 0;

  // Mathematical and logical operators
  const discountAmount = original * (discountPercent / 100);
  const finalPrice = original - discountAmount;
  const savingsGrade = (discountPercent >= 20) ? 'HIGH SAVINGS' : 'REGULAR PRICE';

  const output = `// JavaScript Variables & Arithmetic Operators
const originalPrice = ${original};
const discount = ${discountPercent}%;
const savings = ${discountAmount.toFixed(2)};
const finalPrice = ${finalPrice.toFixed(2)};
const status = "${savingsGrade}";

>> Calculated Final Price: ₹${finalPrice.toFixed(2)} (${savingsGrade})`;

  document.getElementById('labCalcOutput').textContent = output;
});

// Demo 2: Conditionals (if/else & switch)
document.getElementById('runConditionBtn').addEventListener('click', () => {
  const condition = document.getElementById('labConditionSelect').value;
  let examApproved = false;
  let warrantyRecommendation = '';

  // Switch statement demonstration
  switch (condition) {
    case 'NEW':
      examApproved = true;
      warrantyRecommendation = 'Full manufacturer warranty eligible.';
      break;
    case 'LIKE NEW':
      examApproved = true;
      warrantyRecommendation = 'Near mint condition, exam invigilator approved.';
      break;
    case 'GOOD':
      examApproved = true;
      warrantyRecommendation = 'Minor exterior marks, fully functional hardware.';
      break;
    case 'ACCEPTABLE':
      examApproved = false;
      warrantyRecommendation = 'Recommend physical testing before semester exams.';
      break;
    default:
      warrantyRecommendation = 'Unclassified condition rating.';
  }

  const output = `// JavaScript Conditionals (switch-case statement)
Condition Evaluated: "${condition}"
Exam Hall Approved: ${examApproved ? 'YES ✅' : 'CAUTION ⚠️'}
Recommendation: ${warrantyRecommendation}`;

  document.getElementById('labConditionOutput').textContent = output;
});

// Demo 3: Loops & Array Methods
document.getElementById('runArrayDemoBtn').addEventListener('click', () => {
  // Higher-order array functions: filter, map, reduce
  const affordableItems = currentProducts.filter(item => item.price < 200);
  const itemNames = affordableItems.map(item => item.title);
  const totalValue = currentProducts.reduce((sum, item) => sum + item.price, 0);
  const averagePrice = (totalValue / currentProducts.length).toFixed(2);

  const output = `// JavaScript Higher-Order Array Methods
Active Catalog Count: ${currentProducts.length} items
Total Marketplace Value: ₹${totalValue}
Average Item Price: ₹${averagePrice}

Affordable Items (< ₹200) Count: ${affordableItems.length}
Items:
${itemNames.map((n, i) => `  ${i + 1}. ${n}`).join('\n')}`;

  document.getElementById('labArrayOutput').textContent = output;
});

// Demo 4: Dynamic DOM Manipulation (Add/Remove items)
document.getElementById('addDomNoteBtn').addEventListener('click', () => {
  const textInput = document.getElementById('domItemText');
  const text = textInput.value.trim();
  if (!text) return;

  const li = document.createElement('li');
  li.textContent = text + ' (Click to remove)';
  li.addEventListener('click', () => {
    li.remove(); // DOM element removal demonstration
  });

  document.getElementById('domDemoList').appendChild(li);
  textInput.value = '';
});

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

document.getElementById('showRegisterBtn').addEventListener('click', () => {
  loginForm.classList.add('hidden');
  registerForm.classList.remove('hidden');
  document.getElementById('authTitle').textContent = 'Join your campus';
  document.getElementById('authSubtitle').textContent = 'Create a student account. New accounts require administrator verification.';
  setAuthMessage('');
});

document.getElementById('showLoginBtn').addEventListener('click', () => {
  registerForm.classList.add('hidden');
  loginForm.classList.remove('hidden');
  document.getElementById('authTitle').textContent = 'Welcome back';
  document.getElementById('authSubtitle').textContent = 'Sign in to browse and trade academic essentials with your campus community.';
  setAuthMessage('');
});

loginForm.addEventListener('submit', async event => {
  event.preventDefault();
  setAuthMessage('');
  const submitButton = loginForm.querySelector('button[type="submit"]');
  submitButton.disabled = true;
  try {
    const result = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('loginEmail').value.trim(),
        password: document.getElementById('loginPassword').value
      })
    });
    authToken = result.token;
    sessionStorage.setItem('marketplaceToken', authToken);
    loginForm.reset();
    showAuthenticatedApp(result.user);
  } catch (error) {
    setAuthMessage(error.message);
  } finally {
    submitButton.disabled = false;
  }
});

registerForm.addEventListener('submit', async event => {
  event.preventDefault();
  setAuthMessage('');
  const submitButton = registerForm.querySelector('button[type="submit"]');
  submitButton.disabled = true;
  try {
    const result = await apiRequest('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        student_id: document.getElementById('registerStudentId').value.trim(),
        full_name: document.getElementById('registerName').value.trim(),
        email: document.getElementById('registerEmail').value.trim(),
        password: document.getElementById('registerPassword').value
      })
    });
    authToken = result.token;
    sessionStorage.setItem('marketplaceToken', authToken);
    registerForm.reset();
    showAuthenticatedApp(result.user);
  } catch (error) {
    setAuthMessage(error.message);
  } finally {
    submitButton.disabled = false;
  }
});

document.getElementById('logoutBtn').addEventListener('click', async () => {
  try {
    await apiRequest('/api/auth/logout', { method: 'POST' });
    sessionStorage.removeItem('marketplaceToken');
    authToken = null;
    showAuthentication();
    setAuthMessage('You have been logged out.', true);
  } catch (error) {
    document.getElementById('itemCountLabel').textContent = `Could not log out: ${error.message}`;
  }
});

async function restoreSession() {
  if (!authToken) return;
  try {
    const result = await apiRequest('/api/auth/me');
    showAuthenticatedApp(result.user);
  } catch (error) {
    if (error.status === 401) {
      sessionStorage.removeItem('marketplaceToken');
      authToken = null;
      showAuthentication();
      setAuthMessage('Your session expired. Please sign in again.');
      return;
    }
    setAuthMessage(`Could not verify your session: ${error.message}`);
  }
}

restoreSession();
