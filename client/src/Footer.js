// Header.js
import React, {useState, useEffect} from 'react';
import './Footer.css';

function Footer() {
    const [showFooter, setShowFooter] = useState(false);
    useEffect(() => {
        const handleScroll = () => {
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight;
            const scrollTop = window.scrollY || document.documentElement.scrollTop;
            
            // Check if we're at the bottom
            const isBottom = windowHeight + scrollTop >= documentHeight - 10; // -10 for buffer
            setShowFooter(isBottom);
        };

        window.addEventListener('scroll', handleScroll);
        
        // Cleanup
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    if (!showFooter) return null; // Don't render anything if not at bottom

    return (
        <footer className = "footer">
            <div className="footer-content">
                < p >© 2024 Veolia. All Rights Reserved.</p>
            </div>
        </footer>
    )
}
export default Footer;