import React from 'react';
import { Routes, Route } from 'react-router-dom';
import SignInPage from './Components/SignInPage';
import MainPage from './Components/MainPage';

function App() {
  return (
    < Routes >
      <Route path="/" element={<MainPage/>} />
      <Route path="/signin" element={<SignInPage/>} />
    </Routes>
  );
}

export default App;
