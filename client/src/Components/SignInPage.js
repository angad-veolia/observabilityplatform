// SignInPage.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './SignInPage.css';

function SignInPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();  // Add this hook

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Form submitted');
        if (username && password) {
            navigate('/main');  // This will navigate to MainPage
        }
    };

    return (
        <div className="signin-container">
            <div className="signin-box">
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
