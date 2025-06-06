// MainPage.js
import React from 'react';
import Header from './src/Header';
import Footer from './src/Footer';
import './MainPage.css';

function MainPage() {
    return (
        <div>
            <Header />
            <div class = "main-content">
                <h1 className = "welcomeMessage">Welcome, Lokesh</h1>
            </div>
            <Footer />
        </div>
    );
}

export default MainPage;
