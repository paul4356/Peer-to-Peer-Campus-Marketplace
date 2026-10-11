// Active State Variables
let currentProducts = [];
let marketplaceCategories = [];
let authToken = sessionStorage.getItem('marketplaceToken');
let currentUser = null;
let selectedCategory = 'ALL';
let searchQuery = '';
let currentSort = 'featured';
const legacyProductImages = {
  'beer_johnston.png': 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=700&auto=format&fit=crop&q=80',
  'bs_grewal.png': 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=700&auto=format&fit=crop&q=80',
  'campus_bike.png': 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=700&auto=format&fit=crop&q=80',
  'casio_calc.png': 'https://images.unsplash.com/photo-1587145820266-a5951ee6f620?w=700&auto=format&fit=crop&q=80',
  'eg_kit.png': 'https://images.unsplash.com/photo-1517842645767-c639042777db?w=700&auto=format&fit=crop&q=80',
  'lab_coat.png': 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=700&auto=format&fit=crop&q=80',
  'multimeter.png': 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=700&auto=format&fit=crop&q=80',
  'roller_scale_15.png': 'https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=700&auto=format&fit=crop&q=80',
  'roller_scale_box.png': 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=700&auto=format&fit=crop&q=80',
  'rounder.png': 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=700&auto=format&fit=crop&q=80'
};
const categoryFallbackImages = {
  bicycles: legacyProductImages['campus_bike.png'],
  books: legacyProductImages['bs_grewal.png'],
  calculators: legacyProductImages['casio_calc.png'],
  electronics: legacyProductImages['multimeter.png'],
  'lab-coats': legacyProductImages['lab_coat.png'],
  'lab-equipment': legacyProductImages['multimeter.png'],
  stationery: legacyProductImages['roller_scale_15.png']
};

async function apiRequest(path, options = {}) {
  const headers = { Accept: 'application/json', ...(options.headers || {}) };
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';
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

function resolveListingImage(imageUrl, categorySlug) {
  const value = String(imageUrl || '').trim();
  if (/^https?:\/\//i.test(value)) return value;

  const filename = value.split(/[\\/]/).pop()?.toLowerCase();
  if (filename && legacyProductImages[filename]) return legacyProductImages[filename];

  const relativeUpload = value.replace(/^\/+/, '');
  if (/^uploads\/items\/[a-f0-9]{32}\.(?:jpg|png|webp)$/i.test(relativeUpload)) {
    return `/${relativeUpload}`;
  }

  return categoryFallbackImages[categorySlug] || categoryFallbackImages.books;
}

function setAuthMessage(message, isSuccess = false) {
  const messageEl = document.getElementById('authMessage');
  messageEl.textContent = message;
  messageEl.classList.toggle('success', isSuccess);
}

function showAuthenticatedApp(user) {
  currentUser = user;
  document.getElementById('authPage').classList.add('hidden');
  document.getElementById('appContainer').classList.remove('hidden');
  document.getElementById('accountControls').classList.remove('hidden');
  document.getElementById('accountName').textContent = `${user.full_name} · ${user.verification_status}`;
  document.getElementById('adminBtn').classList.toggle('hidden', user.role !== 'admin');
  showWorkspace('marketplace');
  loadMarketplace().catch(error => {
    document.getElementById('itemCountLabel').textContent = `Could not load marketplace: ${error.message}`;
  });
}

function showAuthentication() {
  document.getElementById('authPage').classList.remove('hidden');
  document.getElementById('appContainer').classList.add('hidden');
  document.getElementById('accountControls').classList.add('hidden');
  document.getElementById('profileSection').classList.add('hidden');
  document.getElementById('adminSection').classList.add('hidden');
  document.getElementById('marketplaceSection').classList.remove('hidden');
  document.getElementById('appContainer').classList.remove('panel-open');
  currentUser = null;
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
    image: resolveListingImage(listing.image_url, listing.category.slug)
  }));
  renderCategories();
  renderProducts();
}

function showWorkspace(name) {
  const isProfile = name === 'profile';
  const isAdmin = name === 'admin';
  document.getElementById('marketplaceSection').classList.toggle('hidden', isProfile || isAdmin);
  document.getElementById('profileSection').classList.toggle('hidden', !isProfile);
  document.getElementById('adminSection').classList.toggle('hidden', !isAdmin);
  document.getElementById('appContainer').classList.toggle('panel-open', isProfile || isAdmin);
  if (isProfile) loadProfile().catch(error => {
    document.getElementById('profileMessage').textContent = `Could not load your profile: ${error.message}`;
  });
  if (isAdmin) loadAdminDashboard().catch(error => {
    document.getElementById('adminMessage').textContent = `Could not load admin dashboard: ${error.message}`;
  });
}

async function loadProfile() {
  if (!currentUser) return;
  document.getElementById('profileFullName').value = currentUser.full_name || '';
  document.getElementById('profileStudentId').value = currentUser.student_id || '';
  document.getElementById('profileEmail').value = currentUser.email || '';
  document.getElementById('profilePhone').value = currentUser.phone || '';
  document.getElementById('profileDepartment').value = currentUser.department || '';
  document.getElementById('profileYear').value = currentUser.year_of_study || '';
  document.getElementById('profilePassword').value = '';
  const status = currentUser.verification_status || 'unknown';
  document.getElementById('profileStatus').textContent =
    `Account status: ${status}${currentUser.role === 'admin' ? ' · Administrator' : ''}`;
  const data = await apiRequest('/api/me/listings');
  const listings = data.listings || [];
  const listingContainer = document.getElementById('myListingsList');
  listingContainer.innerHTML = listings.length
    ? listings.map(listing => `
      <article class="panel-list-item">
        <div><strong>${escapeHtml(listing.title)}</strong><span>${escapeHtml(listing.category_name)} · ₹${escapeHtml(listing.price)}</span></div>
        <span class="listing-status">${escapeHtml(listing.status)}</span>
      </article>`).join('')
    : '<p>You have not posted any listings yet.</p>';
}

async function loadAdminDashboard() {
  if (!currentUser || currentUser.role !== 'admin') return;
  const [dashboard, verificationData] = await Promise.all([
    apiRequest('/api/admin/dashboard'),
    apiRequest('/api/admin/verifications')
  ]);
  const metrics = dashboard.metrics || {};
  document.getElementById('adminMetrics').innerHTML = [
    ['Students', metrics.total_users],
    ['Pending approvals', metrics.pending_verifications],
    ['Listings', metrics.total_listings],
    ['Reports', metrics.total_reports]
  ].map(([label, value]) => `
    <div class="metric-card"><span>${escapeHtml(label)}</span><strong>${Number(value || 0)}</strong></div>
  `).join('');

  const requests = verificationData.verifications || [];
  const list = document.getElementById('pendingVerifications');
  list.innerHTML = requests.length
    ? requests.map(student => `
      <article class="panel-list-item verification-item">
        <div>
          <strong>${escapeHtml(student.full_name)}</strong>
          <span>${escapeHtml(student.student_id)} · ${escapeHtml(student.email)}</span>
          <span>${escapeHtml(student.department || 'Department not provided')}</span>
        </div>
        <div class="verification-actions">
          <button type="button" class="primary-btn" data-verification="approved" data-user-id="${Number(student.id)}">Approve</button>
          <button type="button" class="danger-btn" data-verification="rejected" data-user-id="${Number(student.id)}">Reject</button>
        </div>
      </article>`).join('')
    : '<p>There are no student accounts waiting for approval.</p>';
}

document.getElementById('profileBtn').addEventListener('click', () => showWorkspace('profile'));
document.getElementById('adminBtn').addEventListener('click', () => showWorkspace('admin'));
document.getElementById('backToMarketplaceBtn').addEventListener('click', () => showWorkspace('marketplace'));
document.getElementById('adminBackBtn').addEventListener('click', () => showWorkspace('marketplace'));

document.getElementById('profileForm').addEventListener('submit', async event => {
  event.preventDefault();
  const button = document.getElementById('saveProfileBtn');
  const message = document.getElementById('profileMessage');
  button.disabled = true;
  message.textContent = '';
  const body = {
    full_name: document.getElementById('profileFullName').value.trim(),
    phone: document.getElementById('profilePhone').value.trim(),
    department: document.getElementById('profileDepartment').value.trim(),
    year_of_study: Number(document.getElementById('profileYear').value)
  };
  const password = document.getElementById('profilePassword').value;
  if (password) body.password = password;
  try {
    const result = await apiRequest('/api/profile', {
      method: 'PUT',
      body: JSON.stringify(body)
    });
    currentUser = result.user;
    document.getElementById('accountName').textContent =
      `${currentUser.full_name} · ${currentUser.verification_status}`;
    message.textContent = 'Profile saved successfully.';
    await loadProfile();
  } catch (error) {
    message.textContent = error.message;
  } finally {
    button.disabled = false;
  }
});

document.getElementById('pendingVerifications').addEventListener('click', async event => {
  const button = event.target.closest('button[data-verification]');
  if (!button) return;
  button.disabled = true;
  const message = document.getElementById('adminMessage');
  message.textContent = '';
  try {
    await apiRequest(`/api/admin/users/${button.dataset.userId}/verification`, {
      method: 'PATCH',
      body: JSON.stringify({
        status: button.dataset.verification,
        reason: button.dataset.verification === 'rejected' ? 'Please contact campus administration for details.' : ''
      })
    });
    message.textContent = `Student ${button.dataset.verification} successfully.`;
    await loadAdminDashboard();
  } catch (error) {
    message.textContent = error.message;
    button.disabled = false;
  }
});

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
const gridSection = document.getElementById('gridSection');
const formSection = document.getElementById('formSection');

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
  [gridTabBtn, formTabBtn].forEach(btn => btn.classList.remove('active'));
  [gridSection, formSection].forEach(sec => sec.classList.add('hidden'));

  activeBtn.classList.add('active');
  activeSection.classList.remove('hidden');
}

gridTabBtn.addEventListener('click', () => switchTab(gridTabBtn, gridSection));
formTabBtn.addEventListener('click', () => switchTab(formTabBtn, formSection));

// ------------------------------------------------------------------------------
// 4. Client-Side Form Validation & DOM Injection
// ------------------------------------------------------------------------------
const listingForm = document.getElementById('listingForm');
const itemImageInput = document.getElementById('itemImage');
const itemImageError = document.getElementById('itemImageError');
const itemImagePreview = document.getElementById('itemImagePreview');
const itemImagePreviewImg = document.getElementById('itemImagePreviewImg');
let itemImagePreviewUrl = null;

itemImageInput.addEventListener('change', () => {
  const file = itemImageInput.files[0];
  itemImageError.textContent = '';
  itemImageInput.classList.remove('invalid');
  if (itemImagePreviewUrl) URL.revokeObjectURL(itemImagePreviewUrl);
  itemImagePreviewUrl = null;

  if (!file) {
    itemImagePreview.classList.add('hidden');
    itemImagePreviewImg.removeAttribute('src');
    return;
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  if (!allowedTypes.includes(file.type) || file.size > 5 * 1024 * 1024) {
    itemImageError.textContent = 'Choose a JPEG, PNG, or WebP image no larger than 5 MB.';
    itemImageInput.classList.add('invalid');
    itemImageInput.value = '';
    itemImagePreview.classList.add('hidden');
    return;
  }

  itemImagePreviewUrl = URL.createObjectURL(file);
  itemImagePreviewImg.src = itemImagePreviewUrl;
  itemImagePreview.classList.remove('hidden');
});

listingForm.addEventListener('reset', () => {
  if (itemImagePreviewUrl) URL.revokeObjectURL(itemImagePreviewUrl);
  itemImagePreviewUrl = null;
  itemImagePreviewImg.removeAttribute('src');
  itemImagePreview.classList.add('hidden');
  itemImageError.textContent = '';
});

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

  const imageFile = itemImageInput.files[0];
  if (!imageFile) {
    itemImageError.textContent = 'Please add a photo of the item.';
    itemImageInput.classList.add('invalid');
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
    const uploadForm = new FormData();
    uploadForm.append('image', imageFile);
    const uploadData = await apiRequest('/api/uploads/listings', {
      method: 'POST',
      body: uploadForm
    });

    await apiRequest('/api/listings', {
      method: 'POST',
      body: JSON.stringify({
        title: titleInput.value.trim(),
        description: descInput.value.trim(),
        price: priceVal,
        category_id: Number(categoryInput.value),
        condition_state: conditionMap[condition],
        image_url: uploadData.image_url
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
