document.addEventListener('DOMContentLoaded', function () {
    initScrollAnimations();
    initHeaderScroll();
    initSubmitOnce();
});

function initSubmitOnce() {
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            var bouton = form.querySelector('.btn-entrainer, .btn-evaluer, .btn-gold[type="submit"]');
            if (!bouton || bouton.disabled) {
                return;
            }
            bouton.disabled = true;
            bouton.dataset.labelOriginal = bouton.textContent;
            bouton.textContent = 'Patientez...';
        });
    });
}

function initScrollAnimations() {
    var elements = document.querySelectorAll('.fade-in, .fade-in-up, .stagger-children > *');
    if (!elements.length || !('IntersectionObserver' in window)) {
        elements.forEach(function (el) { el.classList.add('is-visible'); });
        return;
    }

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });

    elements.forEach(function (el, index) {
        if (el.parentElement && el.parentElement.classList.contains('stagger-children')) {
            el.style.animationDelay = (index * 0.08) + 's';
        }
        observer.observe(el);
    });
}

function initHeaderScroll() {
    var header = document.querySelector('.site-header');
    if (!header) return;
    window.addEventListener('scroll', function () {
        header.classList.toggle('scrolled', window.scrollY > 24);
    }, { passive: true });
}
