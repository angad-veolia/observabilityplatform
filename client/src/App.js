
import Header from './src/Header';
import Footer from './src/Footer';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import SignIn from './src/SignInPage';
import MainPage from './src/MainPage'; // Your existing page

function App() {
   return (
    < Router >
            < Routes >
                <Route path="/" element={<SignIn />} />
                <Route path="/main" element={<MainPage />} />
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </Router>        
    );
}

export default App;
