export default function AuthShell({ children }) {
  return (
    <div className="entrada">
      <section className="hero">
        <h1>Cada entrega, en su lugar.</h1>
        <p>Un solo sistema para negocios, repartidores y clientes de Oaxaca.</p>
      </section>
      <section className="roles">
        <div className="caja">{children}</div>
      </section>
    </div>
  );
}
