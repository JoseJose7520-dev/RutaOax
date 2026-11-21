import { useEffect, useState } from 'react';

export default function useDebounce(valor, ms = 400) {
  const [retrasado, setRetrasado] = useState(valor);
  useEffect(() => {
    const temporizador = setTimeout(() => setRetrasado(valor), ms);
    return () => clearTimeout(temporizador);
  }, [valor, ms]);
  return retrasado;
}
