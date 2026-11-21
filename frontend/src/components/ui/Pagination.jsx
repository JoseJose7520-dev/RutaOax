export default function Pagination({ page, totalPages, totalElements, onPage }) {
  if (!totalElements) return null;
  const paginas = Math.max(totalPages, 1);
  return (
    <nav className="paginacion" aria-label="Paginación">
      <small>
        {totalElements} {totalElements === 1 ? 'resultado' : 'resultados'}
      </small>
      <div className="fila-g">
        <button type="button" className="btn sec sm" disabled={page <= 0} onClick={() => onPage(page - 1)}>
          Anterior
        </button>
        <span>
          Página {page + 1} de {paginas}
        </span>
        <button type="button" className="btn sec sm" disabled={page + 1 >= paginas} onClick={() => onPage(page + 1)}>
          Siguiente
        </button>
      </div>
    </nav>
  );
}
