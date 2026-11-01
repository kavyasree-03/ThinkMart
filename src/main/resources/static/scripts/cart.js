/* ============================================
   THINK MART – CART LOGIC (Spring Boot API)
   ============================================ */

// ---- Get/create a session ID per browser ----
function getSessionId() {
  let id = localStorage.getItem('thinkmart_session');
  if (!id) {
    id = 'sess-' + Math.random().toString(36).substring(2, 12) + Date.now();
    localStorage.setItem('thinkmart_session', id);
  }
  return id;
}

// ---- Toast notification ----
function showToast(message, isError = false) {
  const toast = document.createElement('div');
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed; bottom: 30px; right: 30px;
    background: ${isError ? '#ef4444' : '#10b981'};
    color: white; padding: 14px 22px; border-radius: 12px;
    font-weight: 600; z-index: 9999;
    box-shadow: 0 12px 24px -6px rgba(0,0,0,0.3);
    font-family: 'Inter', sans-serif;
    animation: slideIn 0.3s ease;
  `;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 2500);
}

// ---- Update cart badge (on nav) ----
async function updateCartBadge() {
  try {
    const res = await fetch(`/api/cart/${getSessionId()}`);
    const items = await res.json();
    const totalQty = items.reduce((sum, item) => sum + item.quantity, 0);
    document.querySelectorAll('.cart-badge').forEach(badge => {
      badge.textContent = totalQty;
      badge.style.display = totalQty > 0 ? 'inline-block' : 'none';
    });
  } catch (e) {
    console.warn('Badge update failed:', e);
  }
}

// ---- Add to cart ----
async function addToCart(productId) {
  try {
    const res = await fetch('/api/cart/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId: getSessionId(),
        productId: productId,
        quantity: 1
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast('✅ Added to cart!');
      updateCartBadge();
    } else {
      showToast(data.message || 'Failed to add', true);
    }
  } catch (e) {
    showToast('Server error. Is Spring Boot running?', true);
    console.error(e);
  }
}

// ---- Render cart page ----
async function renderCartPage() {
  const container = document.getElementById('cartItems');
  if (!container) return; // not on Cart page

  const summaryEl = document.getElementById('orderSummary');

  try {
    const sessionId = getSessionId();
    const [itemsRes, summaryRes] = await Promise.all([
      fetch(`/api/cart/${sessionId}`),
      fetch(`/api/cart/summary/${sessionId}`)
    ]);
    const items = await itemsRes.json();
    const summary = await summaryRes.json();

    // Empty cart
    if (items.length === 0) {
      container.innerHTML = `
        <div style="text-align:center; padding:4rem 2rem; background:#fff; border-radius:20px; border:1px solid #f1f5f9;">
          <i class="fas fa-shopping-cart" style="font-size:3rem; color:#cbd5e1; margin-bottom:1rem;"></i>
          <h3 style="color:#334155; margin-bottom:0.5rem;">Your cart is empty</h3>
          <p style="color:#64748b; margin-bottom:1.5rem;">Add some products to get started!</p>
          <a href="Products.html" class="btn-primary" style="display:inline-block; text-decoration:none; width:auto; padding:12px 28px;">Browse Products</a>
        </div>`;
    } else {
      container.innerHTML = items.map(item => `
        <div class="cart-item" data-id="${item.id}">
          <img src="${item.imageUrl}" alt="${item.title}">
          <div class="cart-item-info">
            <div class="cat">${item.category}</div>
            <h4>${item.title}</h4>
            <div class="price">$${item.price.toLocaleString()}</div>
            <div class="qty-controls">
              <button class="qty-minus">−</button>
              <span>${item.quantity}</span>
              <button class="qty-plus">+</button>
            </div>
          </div>
          <button class="remove-btn"><i class="fas fa-trash"></i></button>
        </div>
      `).join('');

      attachCartHandlers();
    }

    // Update summary numbers
    if (summaryEl) {
      summaryEl.innerHTML = `
        <h3>Order Summary</h3>
        <div class="summary-row"><span>Subtotal</span><span>$${summary.subtotal.toFixed(2)}</span></div>
        <div class="summary-row"><span>Shipping</span><span>Free</span></div>
        <div class="summary-row"><span>Tax (8%)</span><span>$${summary.tax.toFixed(2)}</span></div>
        <div class="summary-row total"><span>Total</span><span>$${summary.total.toFixed(2)}</span></div>
        <button class="checkout-btn" id="checkoutBtn">
          <i class="fas fa-lock"></i> Proceed to Checkout
        </button>
      `;

      // Re-attach checkout handler after re-render
      document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);
    }

    updateCartBadge();

  } catch (e) {
    console.error(e);
    container.innerHTML = '<p style="color:#ef4444;">Failed to load cart. Is the backend running?</p>';
  }
}

// ---- Quantity + / − / Remove handlers ----
function attachCartHandlers() {
  document.querySelectorAll('.cart-item').forEach(row => {
    const id = Number(row.dataset.id);
    const qtySpan = row.querySelector('.qty-controls span');

    row.querySelector('.qty-plus').addEventListener('click', async () => {
      const newQty = Number(qtySpan.textContent) + 1;
      await updateQty(id, newQty);
    });

    row.querySelector('.qty-minus').addEventListener('click', async () => {
      const newQty = Number(qtySpan.textContent) - 1;
      await updateQty(id, newQty);
    });

    row.querySelector('.remove-btn').addEventListener('click', async () => {
      await fetch(`/api/cart/remove/${getSessionId()}/${id}`, { method: 'DELETE' });
      renderCartPage();
    });
  });
}

async function updateQty(productId, quantity) {
  try {
    await fetch('/api/cart/update', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId: getSessionId(),
        productId: productId,
        quantity: quantity
      })
    });
    renderCartPage();
  } catch (e) {
    showToast('Failed to update quantity', true);
    console.error(e);
  }
}

// ---- Checkout handler ----
async function handleCheckout() {
  const sessionId = getSessionId();
  try {
    const res = await fetch(`/api/cart/clear/${sessionId}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      alert('✅ Order placed successfully!\nThank you for shopping with Think Mart.');
      window.location.href = 'Products.html';
    } else {
      alert('❌ Checkout failed. Please try again.');
    }
  } catch (e) {
    alert('Server error. Is Spring Boot running?');
    console.error(e);
  }
}

// ---- Init on every page ----
document.addEventListener('DOMContentLoaded', () => {
  updateCartBadge();
  renderCartPage();
});