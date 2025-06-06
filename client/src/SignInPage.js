// SignInPage.js
import React, {useState} from 'react';
import { useNavigate } from 'react-router-dom';
import './SignInPage.css';
import logoVeolia2 from './logoVeolia2.png'; // Make sure to import your logo

function SignInPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        // Add your authentication logic here
        if (username && password) {
            // If login successful, navigate to main page
            navigate('/main');
        }
    };

    return (
        <div className="signin-container">
            <div className="signin-box">
                <img src={logoVeolia2} alt="Veolia" className="signin-logo" />
                < h2 >Sign In</h2>
                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Username"
                            required
                        />
                    </div>
                    <div className="input-group">
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Password"
                            required
                        />
                    </div>
                    <button type="submit" className="signin-button">
                        Sign In
                    </button>
                </form>
            </div>
        </div>
    );
}

export default SignInPage;