/**
 * TelcoRec SPA - calls the JAX-RS back-end at /reconciliator/api/
 */
'use strict';

const API_BASE = 'http://localhost:8080/reconciliator/api';

// Tab routing
const TAB_LOADERS = {
  dashboard:      loadStats,
  customers:      loadCustomers,
  invoices:       loadInvoices,
  reconciliation: loadResults
};

function activateTab(name) {
  document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.telco-tabs .nav-link').forEach(a => a.classList.remove('active'));
  var pane = document.getElementById('tab-' + name);
  var link = document.querySelector('[data-tab="' + name + '"]');
  if (pane) pane.classList.add('active');
  if (link) link.classList.add('active');
  if (TAB_LOADERS[name]) TAB_LOADERS[name]();
}

function routeFromHash() {
  var tab = (window.location.hash || '#dashboard').replace('#', '') || 'dashboard';
  activateTab(tab);
}

document.querySelectorAll('.telco-tabs .nav-link').forEach(function(link) {
  link.addEventListener('click', function(e) {
    e.preventDefault();
    window.location.hash = '#' + link.getAttribute('data-tab');
  });
});
window.addEventListener('hashchange', routeFromHash);
window.addEventListener('DOMContentLoaded', routeFromHash);
document.getElementById('invoiceStatusFilter').addEventListener('change', loadInvoices);

// API helpers
async function apiFetch(path, opts) {
  var res = await fetch(API_BASE + path, opts || {});
  if (!res.ok) {
    var msg = 'HTTP ' + res.status;
    try { var e = await res.json(); msg = e.error || msg; } catch(x) {}
    throw new Error(msg);
  }
  return res.json();
}

function setSpinner(id, on) {
  var el = document.getElementById('spinner-' + id);
  if (el) el.style.display = on ? 'inline-block' : 'none';
}

function showToast(msg, type) {
  var c = document.getElementById('toastContainer');
  var bg = type === 'error' ? '#b71c1c' : '#00695c';
  var d = document.createElement('div');
  d.className = 'toast align-items-center show border-0';
  d.style.cssText = 'background:' + bg + ';color:#fff;min-width:260px;border-radius:8px';
  d.innerHTML = '<div class="d-flex"><div class="toast-body">' + msg + '</div>' +
    '<button type="button" class="btn-close btn-close-white me-2 m-auto"' +
    ' onclick="this.closest(\'.toast\').remove()"></button></div>';
  c.appendChild(d);
  setTimeout(function() { d.remove(); }, 5000);
}

function statusBadge(v) {
  var cls = 'badge-' + (v || '').toLowerCase().replace(/_/g, '-');
  return '<span class="status-badge ' + cls + '">' + (v || '-') + '</span>';
}

function fmt(v) { return v != null ? parseFloat(v).toFixed(2) : '-'; }

// Dashboard
async function loadStats() {
  try {
    var s = await apiFetch('/reconciliation/stats');
    document.getElementById('stat-total').textContent        = s.totalInvoices || 0;
    document.getElementById('stat-matched').textContent      = s.count_MATCHED || 0;
    var disc = (s.count_OVERCHARGED || 0) + (s.count_UNDERCHARGED || 0) + (s.count_MISSING_CONSUMPTION || 0);
    document.getElementById('stat-discrepancy').textContent  = disc;
    document.getElementById('stat-amount').textContent       = '\u20ac ' + fmt(s.totalDiscrepancyAmount);
    document.getElementById('stat-unprocessed').textContent  = s.unprocessedInvoices || 0;
    document.getElementById('stat-overcharged').textContent  = s.count_OVERCHARGED || 0;
    document.getElementById('stat-undercharged').textContent = s.count_UNDERCHARGED || 0;
    document.getElementById('stat-missing').textContent      = s.count_MISSING_CONSUMPTION || 0;
  } catch(err) { showToast('Errore statistiche: ' + err.message, 'error'); }
}

// Customers
async function loadCustomers() {
  setSpinner('customers', true);
  try {
    var data = await apiFetch('/customers');
    var tbody = document.getElementById('customersBody');
    if (!data.length) { tbody.innerHTML = '<tr><td colspan="8" class="text-center">Nessun cliente.</td></tr>'; return; }
    tbody.innerHTML = data.map(function(c) {
      return '<tr>' +
        '<td><code>' + (c.customerId||'') + '</code></td>' +
        '<td>' + (c.fullName||'') + '</td>' +
        '<td>' + (c.email||'') + '</td>' +
        '<td>' + (c.phoneNumber||'') + '</td>' +
        '<td>' + (c.city||'') + '</td>' +
        '<td><code>' + (c.servicePlan||'') + '</code></td>' +
        '<td>' + statusBadge(c.status) + '</td>' +
        '<td>' + (c.contractStartDate||'') + '</td>' +
        '</tr>';
    }).join('');
  } catch(err) { showToast('Errore clienti: ' + err.message, 'error'); }
  finally { setSpinner('customers', false); }
}

// Invoices
async function loadInvoices() {
  setSpinner('invoices', true);
  var filter = document.getElementById('invoiceStatusFilter').value;
  var qs = filter ? '?reconciliationStatus=' + filter : '';
  try {
    var data = await apiFetch('/invoices' + qs);
    var tbody = document.getElementById('invoicesBody');
    if (!data.length) { tbody.innerHTML = '<tr><td colspan="6" class="text-center">Nessuna fattura.</td></tr>'; return; }
    tbody.innerHTML = data.map(function(inv) {
      var custId = inv.customer ? inv.customer.customerId : '-';
      return '<tr>' +
        '<td><code>' + (inv.invoiceNumber||'') + '</code></td>' +
        '<td>' + custId + '</td>' +
        '<td>' + (inv.billingPeriodStart||'') + ' - ' + (inv.billingPeriodEnd||'') + '</td>' +
        '<td class="text-end">&euro; ' + fmt(inv.totalAmount) + '</td>' +
        '<td>' + statusBadge(inv.status) + '</td>' +
        '<td>' + statusBadge(inv.reconciliationStatus) + '</td>' +
        '</tr>';
    }).join('');
  } catch(err) { showToast('Errore fatture: ' + err.message, 'error'); }
  finally { setSpinner('invoices', false); }
}

// Reconciliation
async function loadResults() {
  setSpinner('reconciliation', true);
  try {
    var data = await apiFetch('/reconciliation/results');
    renderResults(data);
  } catch(err) { showToast('Errore risultati: ' + err.message, 'error'); }
  finally { setSpinner('reconciliation', false); }
}

function renderResults(data) {
  var tbody = document.getElementById('reconciliationBody');
  if (!data.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nessun risultato disponibile.</td></tr>';
    return;
  }
  tbody.innerHTML = data.map(function(r) {
    var invNum = r.invoice ? r.invoice.invoiceNumber : '-';
    var custId = r.invoice && r.invoice.customer ? r.invoice.customer.customerId : '-';
    var diffCls = parseFloat(r.discrepancyAmount) !== 0 ? ' text-danger fw-bold' : '';
    var ts = r.processedAt ? r.processedAt.replace('T',' ').substring(0,19) : '-';
    return '<tr>' +
      '<td><code>' + invNum + '</code></td>' +
      '<td>' + custId + '</td>' +
      '<td class="text-end">&euro; ' + fmt(r.invoicedAmount) + '</td>' +
      '<td class="text-end">&euro; ' + fmt(r.expectedAmount) + '</td>' +
      '<td class="text-end' + diffCls + '">&euro; ' + fmt(r.discrepancyAmount) + '</td>' +
      '<td>' + statusBadge(r.status) + '</td>' +
      '<td>' + ts + '</td>' +
      '</tr>';
  }).join('');
}

async function runReconciliation() {
  var btn = document.getElementById('btnRunAll');
  btn.disabled = true;
  btn.textContent = 'Elaborazione in corso...';
  setSpinner('reconciliation', true);
  try {
    var results = await apiFetch('/reconciliation/run/all', { method: 'POST' });
    showToast('Riconciliazione completata: ' + results.length + ' fatture elaborate.', 'success');
    renderResults(results);
    loadStats();
  } catch(err) { showToast('Errore riconciliazione: ' + err.message, 'error'); }
  finally {
    btn.disabled = false;
    btn.textContent = '\u25b6 Avvia Riconciliazione Completa';
    setSpinner('reconciliation', false);
  }
}
