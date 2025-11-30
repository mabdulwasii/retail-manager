// Shop Manager Landing Page Scripts

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        const href = this.getAttribute('href');
        if (href === '#') return;

        e.preventDefault();
        const target = document.querySelector(href);
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Detect user's operating system and highlight appropriate download
function detectOS() {
    const userAgent = navigator.userAgent.toLowerCase();
    let os = 'unknown';

    if (userAgent.indexOf('win') !== -1) {
        os = 'windows';
    } else if (userAgent.indexOf('mac') !== -1) {
        os = 'macos';
    } else if (userAgent.indexOf('linux') !== -1) {
        os = 'linux';
    }

    return os;
}

// Highlight the appropriate download button based on OS
window.addEventListener('DOMContentLoaded', () => {
    const os = detectOS();
    const downloadCards = document.querySelectorAll('.download-card');

    downloadCards.forEach(card => {
        const cardText = card.querySelector('h3').textContent.toLowerCase();

        if (cardText.includes(os)) {
            card.style.border = '2px solid var(--primary-color)';
            card.style.boxShadow = '0 10px 20px rgba(102, 126, 234, 0.2)';

            // Add "Recommended for your system" badge
            const badge = document.createElement('div');
            badge.style.cssText = `
                position: absolute;
                top: -12px;
                right: 20px;
                background: var(--success-color);
                color: white;
                padding: 6px 12px;
                border-radius: 16px;
                font-size: 12px;
                font-weight: 600;
            `;
            badge.textContent = 'Recommended';
            card.style.position = 'relative';
            card.appendChild(badge);
        }
    });
});

// Simple analytics (you can replace with Google Analytics or similar)
function trackDownload(platform) {
    console.log(`Download initiated for: ${platform}`);
    // Add your analytics tracking code here
    // Example: gtag('event', 'download', { platform: platform });
}

// Add download tracking to buttons
document.querySelectorAll('.download-card .btn-primary').forEach((button, index) => {
    button.addEventListener('click', function(e) {
        const platform = this.closest('.download-card').querySelector('h3').textContent;
        trackDownload(platform);
    });
});

// Navbar scroll effect
let lastScroll = 0;
const navbar = document.querySelector('.navbar');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll > 100) {
        navbar.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    } else {
        navbar.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
    }

    lastScroll = currentScroll;
});

// Add animation on scroll (simple intersection observer)
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all feature cards, pricing cards, etc.
document.addEventListener('DOMContentLoaded', () => {
    const elements = document.querySelectorAll('.feature-card, .pricing-card, .testimonial-card');
    elements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
});

// FAQ Toggle (if you add an FAQ section)
function toggleFAQ(element) {
    const answer = element.nextElementSibling;
    const isOpen = answer.style.maxHeight;

    // Close all other FAQs
    document.querySelectorAll('.faq-answer').forEach(item => {
        item.style.maxHeight = null;
    });

    // Toggle current FAQ
    if (!isOpen) {
        answer.style.maxHeight = answer.scrollHeight + 'px';
    }
}

// Newsletter signup (example)
function subscribeNewsletter(email) {
    // Add your newsletter signup logic here
    console.log('Newsletter signup:', email);
    alert('Thank you for subscribing!');
}

// Add event listener for newsletter form if it exists
const newsletterForm = document.querySelector('#newsletter-form');
if (newsletterForm) {
    newsletterForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = e.target.querySelector('input[type="email"]').value;
        subscribeNewsletter(email);
    });
}

// Platform feature detection and dynamic content
function updateContentForPlatform() {
    const os = detectOS();
    const installText = document.querySelector('.install-step-text');

    if (installText) {
        if (os === 'windows') {
            installText.textContent = 'Double-click the .exe file and follow the installation wizard';
        } else if (os === 'macos') {
            installText.textContent = 'Open the .dmg file and drag Shop Manager to Applications';
        } else if (os === 'linux') {
            installText.textContent = 'Make the AppImage executable and run it: chmod +x Shop-Manager.AppImage';
        }
    }
}

// Call on page load
window.addEventListener('DOMContentLoaded', updateContentForPlatform);

// Add copy-to-clipboard functionality for code snippets
document.querySelectorAll('code').forEach(codeBlock => {
    codeBlock.style.cursor = 'pointer';
    codeBlock.title = 'Click to copy';

    codeBlock.addEventListener('click', () => {
        navigator.clipboard.writeText(codeBlock.textContent);

        // Show feedback
        const originalText = codeBlock.textContent;
        codeBlock.textContent = 'Copied!';
        setTimeout(() => {
            codeBlock.textContent = originalText;
        }, 1000);
    });
});

// Video modal (if you add demo videos)
function openVideoModal(videoUrl) {
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.9);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
    `;

    const iframe = document.createElement('iframe');
    iframe.src = videoUrl;
    iframe.style.cssText = `
        width: 90%;
        max-width: 1200px;
        height: 80%;
        border: none;
    `;

    modal.appendChild(iframe);
    modal.addEventListener('click', () => {
        modal.remove();
    });

    document.body.appendChild(modal);
}

// Add click handlers for demo buttons
document.querySelectorAll('a[href="#demo"]').forEach(btn => {
    btn.addEventListener('click', (e) => {
        e.preventDefault();
        // Replace with your actual video URL
        openVideoModal('https://www.youtube.com/embed/your-video-id');
    });
});
