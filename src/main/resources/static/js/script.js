document.addEventListener('DOMContentLoaded', function() {
    console.log('Selfwise Library Management System loaded');

    initializeSearch();
    initializeFormValidation();
    initializeDeleteConfirmation();
    initializeTooltips();
    initializeAlerts();

    setActiveNavigation();
});

function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchForm = document.getElementById('searchForm');

    if (searchInput && searchForm) {
        let searchTimeout;

        searchInput.addEventListener('input', () => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                if (searchInput.value.length >= 3 || searchInput.value.length === 0) {
                    searchForm.submit();
                }
            }, 500);
        });

        const clearBtn = document.getElementById('clearSearch');
        if (clearBtn) {
            clearBtn.addEventListener('click', e => {
                e.preventDefault();
                searchInput.value = '';
                searchForm.submit();
            });
        }
    }
}

function initializeFormValidation() {
    const forms = document.querySelectorAll('form[data-validate="true"]');

    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(form)) {
                e.preventDefault();
                showAlert('Please correct the errors in the form', 'danger');
            }
        });

        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', () => validateField(input));
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    validateField(input);
                }
            });
        });
    });
}

function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
    inputs.forEach(input => {
        if (!validateField(input)) {
            isValid = false;
        }
    });
    return isValid;
}

function validateField(field) {
    const value = field.value.trim();
    const type = field.type;
    const name = field.name;
    let valid = true;
    let error = '';

    if (field.hasAttribute('required') && !value) {
        valid = false;
        error = `${getFieldLabel(field)} is required`;
    } else if (type === 'email' && value) {
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailPattern.test(value)) {
            valid = false;
            error = 'Please enter a valid email address';
        }
    } else if (type === 'number' && value) {
        if (isNaN(value) || parseFloat(value) < 0) {
            valid = false;
            error = 'Please enter a valid positive number';
        }
    } else if (name === 'isbn' && value) {
        const isbnPattern = /^(?:\d{10}|\d{13})$/;
        if (!isbnPattern.test(value.replace(/-/g, ''))) {
            valid = false;
            error = 'Please enter a valid ISBN (10 or 13 digits)';
        }
    } else if (name === 'studentId' && value) {
        if (value.length < 3) {
            valid = false;
            error = 'Student ID must be at least 3 characters long';
        }
    }

    if (valid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
        hideFieldError(field);
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
        showFieldError(field, error);
    }
    return valid;
}

function getFieldLabel(field) {
    const label = document.querySelector(`label[for="${field.id}"]`);
    return label ? label.textContent.replace('*', '').trim() : field.name;
}

function showFieldError(field, message) {
    let errorDiv = field.parentNode.querySelector('.invalid-feedback');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        field.parentNode.appendChild(errorDiv);
    }
    errorDiv.textContent = message;
}

function hideFieldError(field) {
    const errorDiv = field.parentNode.querySelector('.invalid-feedback');
    if (errorDiv) {
        errorDiv.remove();
    }
}

function initializeDeleteConfirmation() {
    const deleteButtons = document.querySelectorAll('.btn-danger[data-confirm="true"]');

    deleteButtons.forEach(button => {
        button.addEventListener('click', e => {
            e.preventDefault();

            const itemName = button.getAttribute('data-item') || 'this item';
            const confirmMessage = `Are you sure you want to delete ${itemName}? This action cannot be undone.`;

            if (confirm(confirmMessage)) {
                if (button.type === 'submit') {
                    button.closest('form').submit();
                } else if (button.href) {
                    window.location.href = button.href;
                }
            }
        });
    });
}

function initializeTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');

    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', showTooltip);
        element.addEventListener('mouseleave', hideTooltip);
    });
}

function showTooltip(e) {
    const element = e.target;
    const text = element.getAttribute('data-tooltip');

    if (!text) return;

    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: rgba(0, 0, 0, 0.75);
        color: white;
        padding: 6px 10px;
        border-radius: 0.375rem;
        font-size: 13px;
        white-space: nowrap;
        z-index: 1000;
        pointer-events: none;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;

    document.body.appendChild(tooltip);

    const rect = element.getBoundingClientRect();

    tooltip.style.left = `${rect.left + rect.width / 2 - tooltip.offsetWidth / 2}px`;
    tooltip.style.top = `${rect.top - tooltip.offsetHeight - 8}px`;

    requestAnimationFrame(() => {
        tooltip.style.opacity = '1';
    });

    element._tooltip = tooltip;
}

function hideTooltip(e) {
    const element = e.target;
    if (element._tooltip) {
        const tooltip = element._tooltip;
        tooltip.style.opacity = '0';
        setTimeout(() => {
            if (tooltip.parentNode) {
                tooltip.parentNode.removeChild(tooltip);
            }
        }, 300);
        element._tooltip = null;
    }
}

function initializeAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => fadeOut(alert), 5000);

        const closeBtn = alert.querySelector('.alert-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => fadeOut(alert));
        }
    });
}

function fadeOut(element) {
    element.style.transition = 'opacity 0.5s ease';
    element.style.opacity = '0';
    setTimeout(() => {
        if (element.parentNode) {
            element.parentNode.removeChild(element);
        }
    }, 500);
}

function showAlert(message, type = 'info', duration = 5000) {
    const alertContainer = document.getElementById('alertContainer') || createAlertContainer();

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.setAttribute('role', 'alert');
    alert.style.opacity = '0';
    alert.style.transition = 'opacity 0.3s ease';

    alert.innerHTML = `
        ${message}
        <button type="button" class="alert-close" aria-label="Close" style="float: right; background: none; border: none; font-size: 18px; cursor: pointer;">&times;</button>
    `;

    alertContainer.appendChild(alert);

    requestAnimationFrame(() => {
        alert.style.opacity = '1';
    });

    const closeBtn = alert.querySelector('.alert-close');
    closeBtn.addEventListener('click', () => {
        fadeOut(alert);
    });

    setTimeout(() => {
        fadeOut(alert);
    }, duration);
}

function createAlertContainer() {
    const container = document.createElement('div');
    container.id = 'alertContainer';
    container.style.position = 'fixed';
    container.style.top = '1rem';
    container.style.right = '1rem';
    container.style.zIndex = '1050';
    container.style.width = '300px';
    container.style.maxWidth = 'calc(100% - 2rem)';
    container.style.display = 'flex';
    container.style.flexDirection = 'column';
    container.style.gap = '0.75rem';
    document.body.appendChild(container);
    return container;
}

function setActiveNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    const currentPath = window.location.pathname;

    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

