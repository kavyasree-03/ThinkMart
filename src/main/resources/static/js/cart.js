document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('.primary-btn, .qty-btn, .secondary-btn');
    buttons.forEach((button) => {
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'translateY(-1px)';
        });
        button.addEventListener('mouseleave', () => {
            button.style.transform = '';
        });
    });
});
