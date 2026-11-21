export default function PaginaPendiente({ titulo }) {
  return (
    <section className="card pagina-pendiente">
      <h1>{titulo}</h1>
      <p className="muted">Esta pantalla se construye en un día posterior del plan.</p>
    </section>
  );
}
