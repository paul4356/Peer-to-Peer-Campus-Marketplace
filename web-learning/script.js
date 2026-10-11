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

// Initial Dataset directly representing the items shown in the user's project screenshot
const initialCatalog = [
  {
    id: 1,
    title: 'CASIO SCIENTIFIC CALCULATOR',
    category: 'ELECTRONICS',
    price: 600,
    condition: 'GOOD',
    seller: 'ATHARVA GOKHALE',
    description: 'Natural textbook display fx-991EX. Dual power solar and battery. Used for Sem 1 and 2 engineering math.',
    image: 'https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 2,
    title: 'LAB COAT',
    category: 'ACCESSORIES',
    price: 160,
    condition: 'GOOD',
    seller: 'JANHAVI',
    description: 'Navy blue workshop apron size 38. Thick cotton material, 3 pockets intact, clean and washed.',
    image: 'https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 3,
    title: 'ROLLER SCALE - 15 CM',
    category: 'ACCESSORIES',
    price: 19,
    condition: 'ACCEPTABLE',
    seller: 'YASH',
    description: '15 cm parallel rolling ruler for engineering graphics drawings and isometric projections.',
    image: 'https://images.unsplash.com/photo-1585336261026-6d65451e5927?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 4,
    title: 'TOOLS FOR ENGINEERING GRAPHICS (SET)',
    category: 'EG MATERIALS',
    price: 235,
    condition: 'NEW',
    seller: 'YASH',
    description: 'Complete EG drawing kit: Set squares, protractor, French curves, mini drafter compass.',
    image: 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 5,
    title: 'MAPED MECHANICAL ROUNDER FOR ENGINEERING GRAPHICS',
    category: 'EG MATERIALS',
    price: 70,
    condition: 'GOOD',
    seller: 'YASH',
    description: 'High precision compass/rounder with gear wheel adjustment screw for accurate circle drafting.',
    image: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 6,
    title: 'ROLLER SCALE - 15 CM (BOX PACKED)',
    category: 'ACCESSORIES',
    price: 30,
    condition: 'LIKE NEW',
    seller: 'YASH',
    description: 'Clear acrylic roller ruler with box. Used only twice for tutorial assignments.',
    image: 'https://images.unsplash.com/photo-1585336261026-6d65451e5927?w=500&auto=format&fit=crop&q=60'
  }
];

// Active State Variables
let currentProducts = [...initialCatalog];
let selectedCategory = 'ALL';
let searchQuery = '';
let currentSort = 'featured';

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
    const matchesCategory = (selectedCategory === 'ALL') || 
      (item.category.toUpperCase() === selectedCategory.toUpperCase());

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
    return a.id - b.id; // featured default
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

    card.innerHTML = `
      <div class="card-image-wrap">
        <img src="${product.image}" alt="${product.title}" class="card-image" loading="lazy">
        <span class="condition-badge ${conditionClass}">${product.condition}</span>
      </div>
      <div class="card-content">
        <span class="card-category">${product.category}</span>
        <div class="card-header-row">
          <h3 class="card-title">${product.title}</h3>
          <span class="card-price">₹${product.price}</span>
        </div>
        <div class="card-seller">BY ${product.seller}</div>
        <div class="card-actions">
          <button class="btn-chat" data-action="chat" data-id="${product.id}">
            💬 CHAT
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
      <h2 style="color:#fff; margin-bottom:0.5rem;">💬 Campus Chat with ${item.seller}</h2>
      <p style="color:#a1a1aa; font-size:0.85rem; margin-bottom:1rem;">Item: <strong>${item.title}</strong> (₹${item.price})</p>
      <div style="background:#121214; padding:0.85rem; border-radius:6px; margin-bottom:1rem; font-size:0.85rem; color:#f4f4f5;">
        <em>"Hi ${item.seller}, is your ${item.title} still available for handover at the campus library?"</em>
      </div>
      <button class="primary-btn" onclick="alert('Demo: Chat message sent to student seller!')">Send Instant Campus Message</button>
    `);
  } else if (action === 'info') {
    openModal(`
      <h2 style="color:#fff; margin-bottom:0.4rem;">${item.title}</h2>
      <div style="display:flex; justify-content:space-between; margin-bottom:1rem;">
        <span style="color:#f26440; font-weight:700;">${item.category}</span>
        <span style="color:#fff; font-size:1.2rem; font-weight:800;">₹${item.price}</span>
      </div>
      <p style="color:#a1a1aa; line-height:1.6; margin-bottom:1rem;">${item.description}</p>
      <div style="background:#202024; padding:0.75rem; border-radius:6px; font-size:0.8rem; color:#f4f4f5;">
        <div><strong>Condition:</strong> ${item.condition}</div>
        <div><strong>Verified Student Seller:</strong> ${item.seller}</div>
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

listingForm.addEventListener('submit', (e) => {
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

  // Seller Name validation
  const sellerInput = document.getElementById('sellerName');
  if (sellerInput.value.trim().length < 2) {
    document.getElementById('sellerNameError').textContent = 'Seller name is required.';
    sellerInput.classList.add('invalid');
    isValid = false;
  }

  // Email validation with Regex
  const emailInput = document.getElementById('sellerEmail');
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(emailInput.value.trim())) {
    document.getElementById('sellerEmailError').textContent = 'Please provide a valid email format (e.g. name@campus.edu).';
    emailInput.classList.add('invalid');
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

  // Construct new listing object
  const newItem = {
    id: Date.now(),
    title: titleInput.value.trim().toUpperCase(),
    category: categoryInput.value,
    price: priceVal,
    condition: condition,
    seller: sellerInput.value.trim().toUpperCase(),
    description: descInput.value.trim(),
    image: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'
  };

  // Prepend to current catalog (State mutation)
  currentProducts.unshift(newItem);

  // Show success alert
  const alertBox = document.getElementById('formSuccessAlert');
  alertBox.classList.remove('hidden');

  setTimeout(() => {
    alertBox.classList.add('hidden');
    listingForm.reset();
    // Switch back to grid view to reveal newly added product
    switchTab(gridTabBtn, gridSection);
    renderProducts();
  }, 1200);
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

// Initial boot render
document.addEventListener('DOMContentLoaded', () => {
  renderProducts();
});
