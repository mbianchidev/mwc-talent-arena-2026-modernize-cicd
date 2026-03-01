/**
 * TelcoRec SPA - calls the JAX-RS back-end at /reconciliator/api/
 * Supports i18n: English (en), Italian (it), Catalan (ca)
 */
'use strict';

const API_BASE = (window.TELCOREC_API_BASE ||
    (window.location.port === '3000'
        ? window.location.origin + '/reconciliator/api'
        : 'http://localhost:8080/reconciliator/api'));

// ---------------------------------------------------------------------------
// i18n translations
// ---------------------------------------------------------------------------
const TRANSLATIONS = {
  en: {
    // Navbar
    navSubtitle: 'Invoice Reconciliation System',
    // Tabs
    tabDashboard: '\u2637 Dashboard',
    tabCustomers: '\uD83D\uDC64 Customers',
    tabInvoices: '\uD83D\uDCC4 Invoices',
    tabReconciliation: '\u2699 Reconciliation',
    // Dashboard
    dashTitle: '\u2637 Reconciliation Summary',
    statTotalInvoices: 'Total Invoices',
    statMatched: '\u2713 Matched',
    statDiscrepancies: '\u26A0 Discrepancies',
    statTotalDiscrepancy: '\u20AC Total Discrepancy',
    statUnprocessed: '\u25CB Unprocessed',
    statOvercharged: '\u2191 Overcharged',
    statUndercharged: '\u2193 Undercharged',
    statMissing: '? Missing Consumption',
    // Customers
    customersTitle: '\uD83D\uDC64 Customers',
    thCustomerId: 'Customer ID',
    thName: 'Name',
    thEmail: 'Email',
    thPhone: 'Phone',
    thCity: 'City',
    thPlan: 'Plan',
    thStatus: 'Status',
    thSince: 'Since',
    noCustomers: 'No customers.',
    loadingCustomers: 'Loading...',
    // Invoices
    invoicesTitle: '\uD83D\uDCC4 Invoices',
    filterAll: 'All statuses',
    filterUnprocessed: 'Unprocessed',
    filterMatched: 'Matched',
    filterDiscrepancy: 'Discrepancy',
    thInvoiceNum: 'Invoice No.',
    thCustomer: 'Customer',
    thPeriod: 'Period',
    thAmount: 'Amount',
    thPaymentStatus: 'Payment Status',
    thRecStatus: 'Reconciliation Status',
    noInvoices: 'No invoices.',
    loadingInvoices: 'Loading...',
    // Reconciliation
    recTitle: '\u2699 Reconciliation',
    btnRunAll: '\u25B6 Run Full Reconciliation',
    btnRunning: 'Processing...',
    thInvoicedAmount: 'Invoiced Amount',
    thExpectedAmount: 'Expected Amount',
    thDiscrepancy: 'Discrepancy',
    thProcessedAt: 'Processed At',
    noResults: 'No results. Run reconciliation.',
    noResultsAvailable: 'No results available.',
    // Errors & toasts
    errStats: 'Statistics error',
    errCustomers: 'Customers error',
    errInvoices: 'Invoices error',
    errResults: 'Results error',
    errReconciliation: 'Reconciliation error',
    errBackendDown: 'Backend unavailable — make sure TomEE is running on port 8080.',
    successReconciliation: 'Reconciliation complete: {0} invoices processed.',
    // Footer
    footer: '\u00A9 2025 TelcoCorp Italia S.r.l. \u2014 All rights reserved',
    // Language selector
    langLabel: 'Language'
  },
  it: {
    navSubtitle: 'Sistema di Riconciliazione Fatture',
    tabDashboard: '\u2637 Dashboard',
    tabCustomers: '\uD83D\uDC64 Clienti',
    tabInvoices: '\uD83D\uDCC4 Fatture',
    tabReconciliation: '\u2699 Riconciliazione',
    dashTitle: '\u2637 Riepilogo Riconciliazione',
    statTotalInvoices: 'Fatture Totali',
    statMatched: '\u2713 Corrispondenti',
    statDiscrepancies: '\u26A0 Discrepanze',
    statTotalDiscrepancy: '\u20AC Totale Discrepanza',
    statUnprocessed: '\u25CB Non Elaborate',
    statOvercharged: '\u2191 Sovrafatturate',
    statUndercharged: '\u2193 Sottofatturate',
    statMissing: '? Consumo Mancante',
    customersTitle: '\uD83D\uDC64 Clienti',
    thCustomerId: 'ID Cliente',
    thName: 'Nome',
    thEmail: 'Email',
    thPhone: 'Telefono',
    thCity: 'Citt\u00E0',
    thPlan: 'Piano',
    thStatus: 'Stato',
    thSince: 'Dal',
    noCustomers: 'Nessun cliente.',
    loadingCustomers: 'Caricamento...',
    invoicesTitle: '\uD83D\uDCC4 Fatture',
    filterAll: 'Tutti gli stati',
    filterUnprocessed: 'Non elaborate',
    filterMatched: 'Corrispondenti',
    filterDiscrepancy: 'Discrepanza',
    thInvoiceNum: 'N. Fattura',
    thCustomer: 'Cliente',
    thPeriod: 'Periodo',
    thAmount: 'Importo',
    thPaymentStatus: 'Stato Pagamento',
    thRecStatus: 'Stato Riconciliazione',
    noInvoices: 'Nessuna fattura.',
    loadingInvoices: 'Caricamento...',
    recTitle: '\u2699 Riconciliazione',
    btnRunAll: '\u25B6 Avvia Riconciliazione Completa',
    btnRunning: 'Elaborazione in corso...',
    thInvoicedAmount: 'Importo Fatturato',
    thExpectedAmount: 'Importo Atteso',
    thDiscrepancy: 'Discrepanza',
    thProcessedAt: 'Elaborato il',
    noResults: 'Nessun risultato. Avviare la riconciliazione.',
    noResultsAvailable: 'Nessun risultato disponibile.',
    errStats: 'Errore statistiche',
    errCustomers: 'Errore clienti',
    errInvoices: 'Errore fatture',
    errResults: 'Errore risultati',
    errReconciliation: 'Errore riconciliazione',
    errBackendDown: 'Backend non raggiungibile \u2014 verificare che TomEE sia in esecuzione sulla porta 8080.',
    successReconciliation: 'Riconciliazione completata: {0} fatture elaborate.',
    footer: '\u00A9 2025 TelcoCorp Italia S.r.l. \u2014 Tutti i diritti riservati',
    langLabel: 'Lingua'
  },
  ca: {
    navSubtitle: 'Sistema de Reconciliaci\u00F3 de Factures',
    tabDashboard: '\u2637 Tauler',
    tabCustomers: '\uD83D\uDC64 Clients',
    tabInvoices: '\uD83D\uDCC4 Factures',
    tabReconciliation: '\u2699 Reconciliaci\u00F3',
    dashTitle: '\u2637 Resum de Reconciliaci\u00F3',
    statTotalInvoices: 'Factures Totals',
    statMatched: '\u2713 Coincidents',
    statDiscrepancies: '\u26A0 Discrep\u00E0ncies',
    statTotalDiscrepancy: '\u20AC Total Discrep\u00E0ncia',
    statUnprocessed: '\u25CB No Processades',
    statOvercharged: '\u2191 Sobrefacturades',
    statUndercharged: '\u2193 Subfacturades',
    statMissing: '? Consum Absent',
    customersTitle: '\uD83D\uDC64 Clients',
    thCustomerId: 'ID Client',
    thName: 'Nom',
    thEmail: 'Correu',
    thPhone: 'Tel\u00E8fon',
    thCity: 'Ciutat',
    thPlan: 'Pla',
    thStatus: 'Estat',
    thSince: 'Des de',
    noCustomers: 'Cap client.',
    loadingCustomers: 'Carregant...',
    invoicesTitle: '\uD83D\uDCC4 Factures',
    filterAll: 'Tots els estats',
    filterUnprocessed: 'No processades',
    filterMatched: 'Coincidents',
    filterDiscrepancy: 'Discrep\u00E0ncia',
    thInvoiceNum: 'N. Factura',
    thCustomer: 'Client',
    thPeriod: 'Per\u00EDode',
    thAmount: 'Import',
    thPaymentStatus: 'Estat Pagament',
    thRecStatus: 'Estat Reconciliaci\u00F3',
    noInvoices: 'Cap factura.',
    loadingInvoices: 'Carregant...',
    recTitle: '\u2699 Reconciliaci\u00F3',
    btnRunAll: '\u25B6 Iniciar Reconciliaci\u00F3 Completa',
    btnRunning: 'Processant...',
    thInvoicedAmount: 'Import Facturat',
    thExpectedAmount: 'Import Esperat',
    thDiscrepancy: 'Discrep\u00E0ncia',
    thProcessedAt: 'Processat el',
    noResults: 'Cap resultat. Inicieu la reconciliaci\u00F3.',
    noResultsAvailable: 'Cap resultat disponible.',
    errStats: 'Error estad\u00EDstiques',
    errCustomers: 'Error clients',
    errInvoices: 'Error factures',
    errResults: 'Error resultats',
    errReconciliation: 'Error reconciliaci\u00F3',
    errBackendDown: 'Backend no disponible \u2014 comproveu que TomEE est\u00E0 en funcionament al port 8080.',
    successReconciliation: 'Reconciliaci\u00F3 completada: {0} factures processades.',
    footer: '\u00A9 2025 TelcoCorp Italia S.r.l. \u2014 Tots els drets reservats',
    langLabel: 'Idioma'
  }
};

var currentLang = localStorage.getItem('telcorec-lang') || 'it';

function t(key) {
  var dict = TRANSLATIONS[currentLang] || TRANSLATIONS['en'];
  return dict[key] || TRANSLATIONS['en'][key] || key;
}

function tFmt(key, args) {
  var s = t(key);
  if (args) {
    for (var i = 0; i < args.length; i++) {
      s = s.replace('{' + i + '}', args[i]);
    }
  }
  return s;
}

function setLanguage(lang) {
  currentLang = lang;
  localStorage.setItem('telcorec-lang', lang);
  updateAllText();
  // Re-render the active tab
  var activeLink = document.querySelector('.telco-tabs .nav-link.active');
  if (activeLink) {
    var tab = activeLink.getAttribute('data-tab');
    if (TAB_LOADERS[tab]) TAB_LOADERS[tab]();
  }
}

/** Updates all static UI text to the current language. */
function updateAllText() {
  var el;
  // Navbar
  el = document.getElementById('navSubtitle'); if (el) el.textContent = t('navSubtitle');
  // Tabs
  el = document.querySelector('[data-tab="dashboard"]'); if (el) el.textContent = t('tabDashboard');
  el = document.querySelector('[data-tab="customers"]'); if (el) el.textContent = t('tabCustomers');
  el = document.querySelector('[data-tab="invoices"]'); if (el) el.textContent = t('tabInvoices');
  el = document.querySelector('[data-tab="reconciliation"]'); if (el) el.textContent = t('tabReconciliation');
  // Dashboard stat labels
  el = document.getElementById('label-total'); if (el) el.textContent = t('statTotalInvoices');
  el = document.getElementById('label-matched'); if (el) el.textContent = t('statMatched');
  el = document.getElementById('label-discrepancy'); if (el) el.textContent = t('statDiscrepancies');
  el = document.getElementById('label-amount'); if (el) el.textContent = t('statTotalDiscrepancy');
  el = document.getElementById('label-unprocessed'); if (el) el.textContent = t('statUnprocessed');
  el = document.getElementById('label-overcharged'); if (el) el.textContent = t('statOvercharged');
  el = document.getElementById('label-undercharged'); if (el) el.textContent = t('statUndercharged');
  el = document.getElementById('label-missing'); if (el) el.textContent = t('statMissing');
  // Dashboard title
  el = document.getElementById('dashTitle'); if (el) el.textContent = t('dashTitle');
  // Customers title
  el = document.getElementById('customersTitle'); if (el) el.textContent = t('customersTitle');
  // Customer table headers
  var custHeaders = document.querySelectorAll('#customersTable thead th');
  if (custHeaders.length >= 8) {
    custHeaders[0].textContent = t('thCustomerId');
    custHeaders[1].textContent = t('thName');
    custHeaders[2].textContent = t('thEmail');
    custHeaders[3].textContent = t('thPhone');
    custHeaders[4].textContent = t('thCity');
    custHeaders[5].textContent = t('thPlan');
    custHeaders[6].textContent = t('thStatus');
    custHeaders[7].textContent = t('thSince');
  }
  // Invoices title & filter
  el = document.getElementById('invoicesTitle'); if (el) el.textContent = t('invoicesTitle');
  var filterOpts = document.querySelectorAll('#invoiceStatusFilter option');
  if (filterOpts.length >= 4) {
    filterOpts[0].textContent = t('filterAll');
    filterOpts[1].textContent = t('filterUnprocessed');
    filterOpts[2].textContent = t('filterMatched');
    filterOpts[3].textContent = t('filterDiscrepancy');
  }
  // Invoice table headers
  var invHeaders = document.querySelectorAll('#invoicesTable thead th');
  if (invHeaders.length >= 6) {
    invHeaders[0].textContent = t('thInvoiceNum');
    invHeaders[1].textContent = t('thCustomer');
    invHeaders[2].textContent = t('thPeriod');
    invHeaders[3].textContent = t('thAmount');
    invHeaders[4].textContent = t('thPaymentStatus');
    invHeaders[5].textContent = t('thRecStatus');
  }
  // Reconciliation title & button
  el = document.getElementById('recTitle'); if (el) el.textContent = t('recTitle');
  el = document.getElementById('btnRunAll');
  if (el && !el.disabled) el.textContent = t('btnRunAll');
  // Reconciliation table headers
  var recHeaders = document.querySelectorAll('#reconciliationTable thead th');
  if (recHeaders.length >= 7) {
    recHeaders[0].textContent = t('thInvoiceNum');
    recHeaders[1].textContent = t('thCustomer');
    recHeaders[2].textContent = t('thInvoicedAmount');
    recHeaders[3].textContent = t('thExpectedAmount');
    recHeaders[4].textContent = t('thDiscrepancy');
    recHeaders[5].textContent = t('thStatus');
    recHeaders[6].textContent = t('thProcessedAt');
  }
  // Footer
  el = document.getElementById('footerText'); if (el) el.innerHTML = t('footer');
  // Language selector label
  el = document.getElementById('langLabel'); if (el) el.textContent = t('langLabel');
  // Mark active language button
  document.querySelectorAll('.lang-btn').forEach(function(btn) {
    btn.classList.toggle('active', btn.getAttribute('data-lang') === currentLang);
  });
  // Update <html lang>
  document.documentElement.lang = currentLang;
}

// ---------------------------------------------------------------------------
// Tab routing
// ---------------------------------------------------------------------------
const TAB_LOADERS = {
  dashboard:      loadStats,
  customers:      loadCustomers,
  invoices:       loadInvoices,
  reconciliation: loadResults
};

function activateTab(name) {
  document.querySelectorAll('.tab-pane').forEach(function(p) { p.classList.remove('active'); });
  document.querySelectorAll('.telco-tabs .nav-link').forEach(function(a) { a.classList.remove('active'); });
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
window.addEventListener('DOMContentLoaded', function() {
  updateAllText();
  routeFromHash();
});
document.getElementById('invoiceStatusFilter').addEventListener('change', loadInvoices);

// Language switcher
document.querySelectorAll('.lang-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    setLanguage(btn.getAttribute('data-lang'));
  });
});

// ---------------------------------------------------------------------------
// API helpers with improved error handling
// ---------------------------------------------------------------------------
function isNetworkError(err) {
  return err instanceof TypeError && err.message === 'Failed to fetch';
}

async function apiFetch(path, opts) {
  var res;
  try {
    res = await fetch(API_BASE + path, opts || {});
  } catch (err) {
    if (isNetworkError(err)) {
      throw new Error(t('errBackendDown'));
    }
    throw err;
  }
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
  d.style.cssText = 'background:' + bg + ';color:#fff;min-width:300px;border-radius:8px;margin-top:8px';
  d.innerHTML = '<div class="d-flex"><div class="toast-body">' + msg + '</div>' +
    '<button type="button" class="btn-close btn-close-white me-2 m-auto"' +
    ' onclick="this.closest(\'.toast\').remove()"></button></div>';
  c.appendChild(d);
  setTimeout(function() { d.remove(); }, 6000);
}

function statusBadge(v) {
  var cls = 'badge-' + (v || '').toLowerCase().replace(/_/g, '-');
  return '<span class="status-badge ' + cls + '">' + (v || '-') + '</span>';
}

function fmt(v) { return v != null ? parseFloat(v).toFixed(2) : '-'; }

// ---------------------------------------------------------------------------
// Dashboard
// ---------------------------------------------------------------------------
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
  } catch(err) { showToast(t('errStats') + ': ' + err.message, 'error'); }
}

// ---------------------------------------------------------------------------
// Customers
// ---------------------------------------------------------------------------
async function loadCustomers() {
  setSpinner('customers', true);
  try {
    var data = await apiFetch('/customers');
    var tbody = document.getElementById('customersBody');
    if (!data.length) { tbody.innerHTML = '<tr><td colspan="8" class="text-center">' + t('noCustomers') + '</td></tr>'; return; }
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
  } catch(err) { showToast(t('errCustomers') + ': ' + err.message, 'error'); }
  finally { setSpinner('customers', false); }
}

// ---------------------------------------------------------------------------
// Invoices
// ---------------------------------------------------------------------------
async function loadInvoices() {
  setSpinner('invoices', true);
  var filter = document.getElementById('invoiceStatusFilter').value;
  var qs = filter ? '?reconciliationStatus=' + filter : '';
  try {
    var data = await apiFetch('/invoices' + qs);
    var tbody = document.getElementById('invoicesBody');
    if (!data.length) { tbody.innerHTML = '<tr><td colspan="6" class="text-center">' + t('noInvoices') + '</td></tr>'; return; }
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
  } catch(err) { showToast(t('errInvoices') + ': ' + err.message, 'error'); }
  finally { setSpinner('invoices', false); }
}

// ---------------------------------------------------------------------------
// Reconciliation
// ---------------------------------------------------------------------------
async function loadResults() {
  setSpinner('reconciliation', true);
  try {
    var data = await apiFetch('/reconciliation/results');
    renderResults(data);
  } catch(err) { showToast(t('errResults') + ': ' + err.message, 'error'); }
  finally { setSpinner('reconciliation', false); }
}

function renderResults(data) {
  var tbody = document.getElementById('reconciliationBody');
  if (!data.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center">' + t('noResultsAvailable') + '</td></tr>';
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
  btn.textContent = t('btnRunning');
  setSpinner('reconciliation', true);
  try {
    var results = await apiFetch('/reconciliation/run/all', { method: 'POST' });
    showToast(tFmt('successReconciliation', [results.length]), 'success');
    renderResults(results);
    loadStats();
  } catch(err) { showToast(t('errReconciliation') + ': ' + err.message, 'error'); }
  finally {
    btn.disabled = false;
    btn.textContent = t('btnRunAll');
    setSpinner('reconciliation', false);
  }
}
