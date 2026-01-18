import './App.css';
import Refree from './components/Refree/Refree';
import Header from './components/Header'; // Import the new header

function App() {
  return (
    <div id="app">
      <Header /> {/* Add Header Here */}
      <Refree />
    </div>
  );
}

export default App;