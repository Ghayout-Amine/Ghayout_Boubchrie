document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.arbre-zoom-panel').forEach(initialiserZoom);
});

function initialiserZoom(panel) {
    var conteneur = panel.querySelector('.arbre-apercu-conteneur');
    var canvas = panel.querySelector('.arbre-zoom-canvas');
    var label = panel.querySelector('.arbre-zoom-label');
    if (!canvas || !label) {
        return;
    }

    var zoomDefaut = parseFloat(panel.getAttribute('data-zoom-default') || '1');
    var zoom = zoomDefaut;
    var minZoom = 0.15;
    var maxZoom = 1.5;
    var pas = 0.1;

    function appliquerZoom() {
        canvas.style.transform = 'none';
        canvas.style.zoom = zoom;
        label.textContent = Math.round(zoom * 100) + '%';
    }

    function mesurerLargeurNaturelle() {
        var ancienZoom = canvas.style.zoom;
        canvas.style.zoom = '1';
        canvas.style.transform = 'none';
        var largeur = canvas.scrollWidth;
        canvas.style.zoom = ancienZoom;
        return largeur;
    }

    function ajusterAuConteneur() {
        if (!conteneur) {
            appliquerZoom();
            return;
        }
        zoom = zoomDefaut;
        appliquerZoom();
        var largeurArbre = mesurerLargeurNaturelle();
        var largeurZone = conteneur.clientWidth - 48;
        if (largeurArbre > 0 && largeurZone > 0 && largeurArbre > largeurZone) {
            zoom = Math.max(minZoom, Math.min(zoomDefaut, largeurZone / largeurArbre));
        }
        appliquerZoom();
    }

    panel.querySelectorAll('[data-zoom-action]').forEach(function (bouton) {
        bouton.addEventListener('click', function () {
            var action = bouton.getAttribute('data-zoom-action');
            if (action === 'in') {
                zoom = Math.min(maxZoom, +(zoom + pas).toFixed(2));
            } else if (action === 'out') {
                zoom = Math.max(minZoom, +(zoom - pas).toFixed(2));
            } else if (action === 'reset') {
                ajusterAuConteneur();
                return;
            }
            appliquerZoom();
        });
    });

    requestAnimationFrame(function () {
        requestAnimationFrame(ajusterAuConteneur);
    });
}
