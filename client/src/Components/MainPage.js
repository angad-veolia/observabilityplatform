import React from 'react';
import Header from './Header.js';
import Footer from './Footer.js';
import './MainPage.css';

function MainPage() {
    return (
        <div className="main-page">
            <Header />
            <div className="main-content">
                <h1 className="welcomeMessage">Welcome, Lokesh</h1>
            </div>
            <Footer />
        </div>
    );
}

export default MainPage;
