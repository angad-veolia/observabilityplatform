// Header.js
import React, {useState} from 'react';
import './Header.css';
import logoVeolia2 from './Headerimages/logoVeolia2.jpg';
import logOut from './Headerimages/logout.png';


function Header() {
    //CHANGE TO GO TO the landing page with the logo being clicked
    const handleLogoClick = () => {
        window.location.reload();
        // OR
        // window.location.href = '/';
    };
    //the navigation bar
    const [isNavOpen, setIsNavOpen] = useState(false);

    const toggleNav = () => {
        setIsNavOpen(!isNavOpen);
    };
    //write the handleLogOut page

    return (
        <>
        <header className="header">
            <div className="hamburger-menu" onClick = {toggleNav}>
                <div className="bar"></div>
                <div className="bar"></div>
                <div className="bar"></div>
            </div>
            <button className="logo-container" onClick={handleLogoClick}>
                <img src={logoVeolia2} alt="Veolia" className="logoVeolia2" />
            </button>
            <div className="spacer"></div>
            <div className="user-profile">
                <div className="user-icon">
                    <i className="fas fa-user"></i>
                </div>
                <span className="user-name">Lokesh P.</span>
            </div>
            <button className = "logo-container"  onClick={handleLogoClick}> 
                <img src = {logOut} alt = "logOut" className = "logoutButton"></img>
            </button>
        </header>
        <nav className={`nav-menu ${isNavOpen ? 'nav-open' : ''}`}>
                < ul >
                    < li ><a href="/">Home</a></li>
                    < li ><a href="/dashboard">Application View</a></li>
                    < li ><a href="/reports">Cluster View</a></li>
                    < li ><a href="/reports">My Favorite</a></li>
                </ul>
        </nav>
       </> 
    );
}

export default Header;
