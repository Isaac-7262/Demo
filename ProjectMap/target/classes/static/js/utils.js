/**
 * Utility Functions for KKU Incident Map
 */

/**
 * แสดง Toast Notification
 */
function showToast(message, type = 'info', duration = 3000) {
    const toastHTML = `
        <div class="toast ${type}">
            <div class="d-flex align-items-center">
                <i class="fas fa-${getToastIcon(type)} me-2"></i>
                <span>${message}</span>
            </div>
        </div>
    `;

    const container = document.querySelector('.toast-container') || createToastContainer();
    const toastEl = document.createElement('div');
    toastEl.innerHTML = toastHTML;
    container.appendChild(toastEl);

    if (duration > 0) {
        setTimeout(() => {
            toastEl.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => toastEl.remove(), 300);
        }, duration);
    }
}

function getToastIcon(type) {
    const icons = {
        'success': 'check-circle',
        'error': 'exclamation-circle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    return icons[type] || 'info-circle';
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
}

/**
 * Format datetime to Thai locale
 */
function formatDateTimeThai(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('th-TH', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * Format date to Thai locale
 */
function formatDateThai(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('th-TH', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

/**
 * Format time to Thai locale
 */
function formatTimeThai(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('th-TH', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * Debounce function for search/filter
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Throttle function for scroll events
 */
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

/**
 * Copy to clipboard
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('คัดลอกสำเร็จ', 'success', 2000);
    }).catch(() => {
        showToast('ไม่สามารถคัดลอกได้', 'error', 2000);
    });
}

/**
 * Generate random ID
 */
function generateRandomId() {
    return 'id_' + Math.random().toString(36).substr(2, 9);
}

/**
 * Check if element is in viewport
 */
function isElementInViewport(el) {
    const rect = el.getBoundingClientRect();
    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
        rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
}

/**
 * Smooth scroll to element
 */
function smoothScrollToElement(element) {
    element.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

/**
 * Format number with Thai currency
 */
function formatThaiCurrency(number) {
    return new Intl.NumberFormat('th-TH', {
        style: 'currency',
        currency: 'THB'
    }).format(number);
}

/**
 * Validate email
 */
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

/**
 * Validate phone number
 */
function validatePhoneNumber(phone) {
    const re = /^[0-9]{10}$/;
    return re.test(phone.replace(/[^0-9]/g, ''));
}

/**
 * Get query parameter from URL
 */
function getQueryParameter(name) {
    const url = new URL(window.location);
    return url.searchParams.get(name);
}

/**
 * Set query parameter in URL
 */
function setQueryParameter(name, value) {
    const url = new URL(window.location);
    url.searchParams.set(name, value);
    window.history.replaceState({}, document.title, url.toString());
}

/**
 * Local Storage wrapper
 */
const StorageManager = {
    set: (key, value) => {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('Storage error:', e);
        }
    },
    get: (key) => {
        try {
            const value = localStorage.getItem(key);
            return value ? JSON.parse(value) : null;
        } catch (e) {
            console.error('Storage error:', e);
            return null;
        }
    },
    remove: (key) => {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.error('Storage error:', e);
        }
    },
    clear: () => {
        try {
            localStorage.clear();
        } catch (e) {
            console.error('Storage error:', e);
        }
    }
};

/**
 * API wrapper with error handling
 */
class ApiClient {
    static async get(endpoint) {
        try {
            const response = await fetch(endpoint);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            showToast('ไม่สามารถดึงข้อมูลได้', 'error');
            throw error;
        }
    }

    static async post(endpoint, data) {
        try {
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            showToast('ไม่สามารถส่งข้อมูลได้', 'error');
            throw error;
        }
    }

    static async put(endpoint, data) {
        try {
            const response = await fetch(endpoint, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            showToast('ไม่สามารถอัปเดตได้', 'error');
            throw error;
        }
    }

    static async delete(endpoint) {
        try {
            const response = await fetch(endpoint, {
                method: 'DELETE'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            showToast('ไม่สามารถลบได้', 'error');
            throw error;
        }
    }
}

/**
 * Logger utility
 */
const Logger = {
    log: (message, data = null) => {
        console.log(`[LOG] ${message}`, data);
    },
    error: (message, error = null) => {
        console.error(`[ERROR] ${message}`, error);
    },
    warn: (message, data = null) => {
        console.warn(`[WARN] ${message}`, data);
    },
    info: (message, data = null) => {
        console.info(`[INFO] ${message}`, data);
    }
};

// Export for use
window.KKUUtils = {
    showToast,
    formatDateTimeThai,
    formatDateThai,
    formatTimeThai,
    debounce,
    throttle,
    copyToClipboard,
    generateRandomId,
    isElementInViewport,
    smoothScrollToElement,
    formatThaiCurrency,
    validateEmail,
    validatePhoneNumber,
    getQueryParameter,
    setQueryParameter,
    StorageManager,
    ApiClient,
    Logger
};
