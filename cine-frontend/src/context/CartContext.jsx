import { createContext, useContext, useState, useCallback } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [items, setItems] = useState([]);

  const addItem = useCallback((funcionId, fila, asiento, movieTitle, fechaHora, precio, salaName) => {
    const exists = items.find(
      (i) => i.funcionId === funcionId && i.fila === fila && i.asiento === asiento
    );
    if (exists) return false;

    setItems((prev) => [
      ...prev,
      { funcionId, fila, asiento, movieTitle, fechaHora, precio, salaName, id: Date.now() },
    ]);
    return true;
  }, [items]);

  const removeItem = useCallback((id) => {
    setItems((prev) => prev.filter((i) => i.id !== id));
  }, []);

  const clearCart = useCallback(() => {
    setItems([]);
  }, []);

  const total = items.reduce((sum, i) => sum + (i.precio || 0), 0);
  const count = items.length;

  return (
    <CartContext.Provider value={{ items, addItem, removeItem, clearCart, total, count }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart debe usarse dentro de CartProvider');
  return context;
}
