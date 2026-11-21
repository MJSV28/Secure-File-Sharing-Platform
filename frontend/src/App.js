
import React, { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8888/api";

function App() {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState("");
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function readError(response) {
    const text = await response.text();
    try {
      const data = JSON.parse(text);
      return data.message || data.error || text;
    } catch {
      return text || `Request failed: ${response.status}`;
    }
  }

  async function handleAuth(event) {
    event.preventDefault();
    setBusy(true);
    setMessage("");

    try {
      const endpoint =
        mode === "register" ? "/auth/register" : "/auth/login";

      const response = await fetch(API + endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      if (mode === "register") {
        setMode("login");
        setPassword("");
        setMessage("Account created. Please log in.");
      } else {
        const data = await response.json();
        setToken(data.token);
        setEmail(data.email);
        setPassword("");
        setMessage("Login successful.");
      }
    } catch (error) {
      setMessage(error.message || "Unable to connect to the server.");
    } finally {
      setBusy(false);
    }
  }

  async function loadFiles() {
    if (!token) return;

    try {
      const response = await fetch(`${API}/files`, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setFiles(await response.json());
    } catch (error) {
      setMessage(error.message || "Unable to load files.");
    }
  }

  useEffect(() => {
    if (token) loadFiles();
  }, [token]);

  async function handleUpload(event) {
    event.preventDefault();

    if (!selectedFile) {
      setMessage("Please select a file.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    setBusy(true);
    setMessage("");

    try {
      const response = await fetch(`${API}/files/upload`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setSelectedFile(null);
      event.target.reset();
      setMessage("File uploaded successfully.");
      await loadFiles();
    } catch (error) {
      setMessage(error.message || "File upload failed.");
    } finally {
      setBusy(false);
    }
  }

  async function handleDownload(file) {
    try {
      const response = await fetch(
        `${API}/files/${file.id}/download`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = url;
      link.download = file.originalFilename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error.message || "Download failed.");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Delete this file permanently?")) return;

    try {
      const response = await fetch(`${API}/files/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setMessage("File deleted successfully.");
      await loadFiles();
    } catch (error) {
      setMessage(error.message || "Delete failed.");
    }
  }

  function logout() {
    setToken("");
    setFiles([]);
    setPassword("");
    setSelectedFile(null);
    setMode("login");
    setMessage("You have logged out.");
  }

  function formatSize(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(2)} MB`;
  }

  if (!token) {
    return (
      <main className="auth-page">
        <section className="auth-card">
          <div className="logo">S</div>
          <h1>SecureShare</h1>
          <p className="muted centered">
            Store and manage your files securely.
          </p>

          <div className="tabs">
            <button
              className={mode === "login" ? "active" : ""}
              onClick={() => {
                setMode("login");
                setMessage("");
              }}
            >
              Login
            </button>
            <button
              className={mode === "register" ? "active" : ""}
              onClick={() => {
                setMode("register");
                setMessage("");
              }}
            >
              Register
            </button>
          </div>

          <form onSubmit={handleAuth}>
            <label>Email address</label>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />

            <label>Password</label>
            <input
              type="password"
              placeholder="At least 8 characters"
              value={password}
              onChange={e => setPassword(e.target.value)}
              minLength={8}
              maxLength={72}
              required
            />

            <button className="primary full" disabled={busy}>
              {busy
                ? "Please wait..."
                : mode === "login"
                ? "Login securely"
                : "Create account"}
            </button>
          </form>

          {message && <p className="message">{message}</p>}

          <p className="security-note">
            JWT authentication · BCrypt password hashing
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard">
      <header className="topbar">
        <div className="brand">
          <div className="logo">S</div>
          <div>
            <h2>SecureShare</h2>
            <span>Secure file management</span>
          </div>
        </div>

        <div className="user-actions">
          <span>{email}</span>
          <button className="secondary" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <section className="welcome">
        <p className="eyebrow">YOUR WORKSPACE</p>
        <h1>Welcome back!</h1>
        <p>Upload and manage your personal files in one place.</p>
      </section>

      {message && <p className="message">{message}</p>}

      <section className="panel upload-panel">
        <div>
          <h2>Upload a file</h2>
          <p className="muted">PDF, PNG, JPG, JPEG, or TXT</p>
          <p className="small muted">Maximum size: 10 MB</p>
        </div>

        <form className="upload-form" onSubmit={handleUpload}>
          <input
            type="file"
            accept=".pdf,.png,.jpg,.jpeg,.txt"
            onChange={e =>
              setSelectedFile(e.target.files[0] || null)
            }
            required
          />
          <button className="primary" disabled={busy}>
            {busy ? "Uploading..." : "Upload file"}
          </button>
        </form>
      </section>

      <section className="files-section">
        <div className="section-title">
          <div>
            <h2>My files</h2>
            <p className="muted">Files uploaded by your account.</p>
          </div>
          <button className="secondary" onClick={loadFiles}>
            Refresh
          </button>
        </div>

        {files.length === 0 ? (
          <div className="empty panel">
            <div className="empty-symbol">↑</div>
            <h3>No files yet</h3>
            <p className="muted">
              Upload your first file to get started.
            </p>
          </div>
        ) : (
          <div className="file-list panel">
            {files.map(file => (
              <article className="file-row" key={file.id}>
                <div className="file-symbol">FILE</div>

                <div className="file-info">
                  <h3>{file.originalFilename}</h3>
                  <p className="muted">
                    {formatSize(file.size)} ·{" "}
                    {file.uploadedAt
                      ? new Date(file.uploadedAt).toLocaleString()
                      : "Date unavailable"}
                  </p>
                </div>

                <div className="file-actions">
                  <button
                    className="download"
                    onClick={() => handleDownload(file)}
                  >
                    Download
                  </button>
                  <button
                    className="delete"
                    onClick={() => handleDelete(file.id)}
                  >
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <footer>SecureShare · Your files, your control.</footer>
    </main>
  );
}

export default App;
