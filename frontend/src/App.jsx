import { useState } from 'react';
import api from './api';
import './App.css';

function App() {
  const [tab, setTab] = useState('book');

  return (
    <div className="app">
      <h1>🏥 Auriga Clinic — Front Desk</h1>

      <nav className="tabs">
        <button className={tab === 'book' ? 'active' : ''} onClick={() => setTab('book')}>Book Appointment</button>
        <button className={tab === 'schedule' ? 'active' : ''} onClick={() => setTab('schedule')}>Doctor Schedule</button>
        <button className={tab === 'search' ? 'active' : ''} onClick={() => setTab('search')}>Find Patient</button>
      </nav>

      {tab === 'book' && <BookAppointment />}
      {tab === 'schedule' && <DoctorSchedule />}
      {tab === 'search' && <PatientSearch />}
    </div>
  );
}

function BookAppointment() {
  const [form, setForm] = useState({ doctorId: '', patientId: '', startTime: '', endTime: '' });
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);
    try {
      const res = await api.post('/appointments', {
        doctorId: Number(form.doctorId),
        patientId: Number(form.patientId),
        startTime: form.startTime,
        endTime: form.endTime,
      });
      setMessage(`✅ Booked! Appointment ID: ${res.data.id}`);
      setForm({ doctorId: '', patientId: '', startTime: '', endTime: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    }
  };

  return (
    <div className="card">
      <h2>Book Appointment</h2>
      <form onSubmit={handleSubmit}>
        <label>Doctor ID</label>
        <input name="doctorId" value={form.doctorId} onChange={handleChange} required />

        <label>Patient ID</label>
        <input name="patientId" value={form.patientId} onChange={handleChange} required />

        <label>Start Time</label>
        <input type="datetime-local" name="startTime" value={form.startTime} onChange={handleChange} required />

        <label>End Time</label>
        <input type="datetime-local" name="endTime" value={form.endTime} onChange={handleChange} required />

        <button type="submit">Book</button>
      </form>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </div>
  );
}

function DoctorSchedule() {
  const [doctorId, setDoctorId] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState(null);

  const fetchSchedule = async () => {
    setError(null);
    try {
      const res = await api.get(`/appointments/doctor/${doctorId}/schedule`);
      setAppointments(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    }
  };

  const cancelAppointment = async (id) => {
    try {
      await api.post(`/appointments/${id}/cancel`, {});
      fetchSchedule();
    } catch (err) {
      setError(err.response?.data?.message || 'Cancel failed');
    }
  };

  return (
    <div className="card">
      <h2>Doctor's Day Schedule</h2>
      <div className="inline-form">
        <input placeholder="Doctor ID" value={doctorId} onChange={(e) => setDoctorId(e.target.value)} />
        <button onClick={fetchSchedule}>View Schedule</button>
      </div>
      {error && <p className="error">{error}</p>}
      <ul className="list">
        {appointments.map((a) => (
          <li key={a.id}>
            <strong>{a.startTime} → {a.endTime}</strong> — {a.patient.name}
            <button className="cancel-btn" onClick={() => cancelAppointment(a.id)}>Cancel</button>
          </li>
        ))}
        {appointments.length === 0 && <li className="empty">No appointments loaded yet</li>}
      </ul>
    </div>
  );
}

function PatientSearch() {
  const [name, setName] = useState('');
  const [patients, setPatients] = useState([]);
  const [error, setError] = useState(null);

  const search = async () => {
    setError(null);
    try {
      const res = await api.get(`/patients/search`, { params: { name } });
      setPatients(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    }
  };

  return (
    <div className="card">
      <h2>Find Patient by Name</h2>
      <div className="inline-form">
        <input placeholder="Patient name" value={name} onChange={(e) => setName(e.target.value)} />
        <button onClick={search}>Search</button>
      </div>
      {error && <p className="error">{error}</p>}
      <ul className="list">
        {patients.map((p) => (
          <li key={p.id}>#{p.id} — {p.name} ({p.phone})</li>
        ))}
        {patients.length === 0 && <li className="empty">No results yet</li>}
      </ul>
    </div>
  );
}

export default App;