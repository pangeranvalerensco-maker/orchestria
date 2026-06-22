import React, { useState, useEffect } from 'react';
import type { 
    SessionDemoResponse, 
    SessionDemoLoginPayload 
} from '../../types/sessionDemo';
import { 
    getSessionDemoStatus, 
    loginSessionDemo, 
    logoutSessionDemo, 
    getSessionDemoProfile 
} from '../../services/sessionDemoService';
import '../../session-demo.css';

const StatefulSessionDemoPage: React.FC = () => {
    const [loading, setLoading] = useState<boolean>(true);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    
    const [sessionData, setSessionData] = useState<SessionDemoResponse | null>(null);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const checkStatus = async () => {
        setLoading(true);
        setError(null);
        try {
            const status = await getSessionDemoStatus();
            if (status.data.authenticated) {
                const profile = await getSessionDemoProfile();
                setSessionData(profile.data);
            } else {
                setSessionData(null);
            }
        } catch (err: unknown) {
            setSessionData(null);
            // Ignore 401 on status/profile check
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        checkStatus();
    }, []);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        const payload: SessionDemoLoginPayload = { email, password };

        try {
            const response = await loginSessionDemo(payload);
            setSessionData(response.data);
            setEmail('');
            setPassword('');
        } catch (err: unknown) {
            const errorObj = err as { message?: string };
            setError(errorObj.message || 'Login gagal. Silakan periksa kembali email dan password Anda.');
        } finally {
            setSubmitting(false);
        }
    };

    const handleLogout = async () => {
        setSubmitting(true);
        try {
            await logoutSessionDemo();
            setSessionData(null);
        } catch (err: unknown) {
            const errorObj = err as { message?: string };
            setError(errorObj.message || 'Gagal melakukan logout session.');
        } finally {
            setSubmitting(false);
        }
    };

    const handleCheckSession = async () => {
        await checkStatus();
    };

    if (loading) {
        return (
            <div className="session-demo-container">
                <div className="session-demo-header">
                    <h2>Demo Materi: Stateful HTTP Session</h2>
                    <p>Memuat status session...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="session-demo-container">
            <div className="session-demo-header">
                <h2>Demo Materi: Stateful HTTP Session</h2>
            </div>
            
            <div className="session-demo-alert">
                <p><strong>Informasi:</strong></p>
                <ul>
                    <li>Halaman ini BUKAN login utama Orchestria.</li>
                    <li>Server menyimpan status autentikasi di dalam memory (stateful).</li>
                    <li>Browser hanya membawa opaque HttpOnly cookie (`ORCHESTRIA_SESSION_DEMO`).</li>
                    <li>Session berakhir secara otomatis setelah tidak aktif selama 15 menit.</li>
                    <li>Login utama Orchestria tetap menggunakan JWT (stateless).</li>
                </ul>
            </div>

            {error && (
                <div className="session-demo-error">
                    {error}
                </div>
            )}

            {!sessionData?.authenticated ? (
                <form onSubmit={handleLogin}>
                    <div className="session-demo-form-group">
                        <label>Email</label>
                        <input 
                            type="email" 
                            value={email} 
                            onChange={(e) => setEmail(e.target.value)} 
                            required 
                            disabled={submitting}
                        />
                    </div>
                    <div className="session-demo-form-group">
                        <label>Password</label>
                        <input 
                            type="password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            required 
                            disabled={submitting}
                        />
                    </div>
                    <button type="submit" className="session-demo-btn" disabled={submitting}>
                        {submitting ? 'Loading...' : 'Login Stateful Session'}
                    </button>
                </form>
            ) : (
                <div className="session-demo-profile">
                    <h3>Profil Session Aktif</h3>
                    <p><strong>Nama:</strong> {sessionData.user?.fullName}</p>
                    <p><strong>Email:</strong> {sessionData.user?.email}</p>
                    <p><strong>Role:</strong> {sessionData.user?.roles.join(', ')}</p>
                    <hr />
                    <p><strong>Mode Autentikasi:</strong> {sessionData.authenticationMode}</p>
                    <p><strong>Dibuat Pada:</strong> {sessionData.createdAt ? new Date(sessionData.createdAt).toLocaleString() : '-'}</p>
                    <p><strong>Akses Terakhir:</strong> {sessionData.lastAccessedAt ? new Date(sessionData.lastAccessedAt).toLocaleString() : '-'}</p>
                    <p><strong>Estimasi Sisa Session:</strong> {sessionData.expiresInSeconds} detik</p>

                    <div className="session-demo-actions">
                        <button 
                            className="session-demo-btn session-demo-btn-secondary" 
                            onClick={handleCheckSession} 
                            disabled={submitting}
                        >
                            Periksa Session
                        </button>
                        <button 
                            className="session-demo-btn session-demo-btn-danger" 
                            onClick={handleLogout} 
                            disabled={submitting}
                        >
                            Logout Session
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default StatefulSessionDemoPage;
