document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing calculator...');

    localStorage.clear();
    console.log('All localStorage data cleared!');

    // ========== ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ==========
    let currentMode = 'matrices';  // 'matrices', 'slae', 'vectors'
    let currentOperation = 'add';
    let currentSlaeMethod = 'gauss';
    let currentVectorOperation = 'vectorAdd';

    let operations = {};
    let slaeMethods = {};
    let vectorOperations = {};

    // Словари операций по режимам (для единого operation-selector)
    const operationsByMode = {
        matrices: {
            'add': 'Сложение матриц',
            'subtract': 'Вычитание матриц',
            'multiply': 'Умножение матриц',
            'multiplyScalar': 'Умножение матрицы на число',
            'transpose': 'Транспонирование',
            'determinant': 'Определитель',
            'rank': 'Ранг',
            'inverse': 'Обратная матрица'
        },
        slae: {
            'slae': 'Решение СЛАУ'
        },
        vectors: {
            'vectorAdd': 'Сложение векторов',
            'vectorSub': 'Вычитание векторов',
            'vectorMultiplyScalar': 'Умножение вектора на число',
            'vectorDot': 'Скалярное произведение',
            'vectorCross': 'Векторное произведение (3D)',
            'vectorMixed': 'Смешанное произведение (3D)'
        }
    };

    // ========== ЗАГРУЗКА ДАННЫХ ==========
    fetch('/api/operations')
        .then(response => response.json())
        .then(data => {
            operations = data;
            renderOperationSelector();
        });

    fetch('/api/slae-methods')
        .then(response => response.json())
        .then(data => {
            slaeMethods = data;
            const select = document.getElementById('slae-method');
            if (select) {
                select.innerHTML = '';
                for (const [value, label] of Object.entries(data)) {
                    const option = document.createElement('option');
                    option.value = value;
                    option.textContent = label;
                    if (value === currentSlaeMethod) option.selected = true;
                    select.appendChild(option);
                }
                select.addEventListener('change', (e) => {
                    currentSlaeMethod = e.target.value;
                });
            }
        });

    fetch('/api/vector/operations')
        .then(response => response.json())
        .then(data => {
            vectorOperations = data;
        });

    // ========== ЕДИНЫЙ OPERATION-SELECTOR ==========
    function renderOperationSelector() {
        const container = document.getElementById('operation-selector');
        if (!container) return;

        const ops = operationsByMode[currentMode];

        let html = '<div class="operation-group">';
        for (const [value, label] of Object.entries(ops)) {
            html += `
                <label class="operation-option">
                    <input type="radio" name="operation" value="${value}"
                           ${value === currentOperation ? 'checked' : ''}>
                    ${label}
                </label>
            `;
        }
        html += '</div>';
        container.innerHTML = html;

        document.querySelectorAll('input[name="operation"]').forEach(radio => {
            radio.addEventListener('change', onOperationChange);
        });

        // Обновляем видимость панелей в зависимости от операции
        if (currentMode === 'matrices') {
            updateMatricesPanelsVisibility();
        } else if (currentMode === 'vectors') {
            updateVectorPanelsVisibility();
        }
    }

    function onOperationChange(e) {
        currentOperation = e.target.value;

        if (currentMode === 'matrices') {
            updateMatricesPanelsVisibility();
        } else if (currentMode === 'vectors') {
            currentVectorOperation = e.target.value;
            updateVectorPanelsVisibility();
        }
    }

    // ========== ПАНЕЛИ МАТРИЦ ==========
    function updateMatricesPanelsVisibility() {
        const matrixBPanel = document.getElementById('matrix-b-panel');
        const scalarPanel = document.getElementById('scalar-panel');

        const binaryOps = ['add', 'subtract', 'multiply'];
        const scalarOps = ['multiplyScalar'];

        if (matrixBPanel) {
            matrixBPanel.style.display = binaryOps.includes(currentOperation) ? 'block' : 'none';
        }
        if (scalarPanel) {
            scalarPanel.style.display = scalarOps.includes(currentOperation) ? 'block' : 'none';
        }
    }

    // ========== ПАНЕЛИ ВЕКТОРОВ ==========
    function updateVectorPanelsVisibility() {
        const vectorsVectorBPanel = document.getElementById('vectors-vector-b-panel');
        const vectorCPanel = document.getElementById('vector-c-panel');
        const vectorScalarPanel = document.getElementById('vector-scalar-panel');

        const isBinary = ['vectorAdd', 'vectorSub', 'vectorDot', 'vectorCross'].includes(currentVectorOperation);
        const isMixed = currentVectorOperation === 'vectorMixed';
        const isScalar = currentVectorOperation === 'vectorMultiplyScalar';

        if (vectorsVectorBPanel) {
            vectorsVectorBPanel.style.display = (isBinary || isMixed) ? 'block' : 'none';
            console.log('Vector B panel visible:', (isBinary || isMixed));
        }
        if (vectorCPanel) vectorCPanel.style.display = isMixed ? 'block' : 'none';
        if (vectorScalarPanel) vectorScalarPanel.style.display = isScalar ? 'block' : 'none';
    }


    // ========== ПЕРЕКЛЮЧЕНИЕ РЕЖИМОВ ==========
    function switchMode(mode) {
        currentMode = mode;

        if (mode === 'matrices') {
            currentOperation = 'add';
        } else if (mode === 'slae') {
            currentOperation = 'slae';
            updateMatrixA();
            updateSlaeVectorB();
        } else if (mode === 'vectors') {
            currentOperation = 'vectorAdd';
            currentVectorOperation = 'vectorAdd';
        }

        renderOperationSelector();

        // Получаем все панели
        const matrixAPanel = document.getElementById('matrix-a-panel');
        const matrixBPanel = document.getElementById('matrix-b-panel');
        const scalarPanel = document.getElementById('scalar-panel');
        const slaePanel = document.getElementById('slae-panel');
        const slaeVectorBPanel = document.getElementById('slae-vector-b-panel');
        const vectorAPanel = document.getElementById('vector-a-panel');
        const vectorsVectorBPanel = document.getElementById('vectors-vector-b-panel');
        const vectorCPanel = document.getElementById('vector-c-panel');
        const vectorScalarPanel = document.getElementById('vector-scalar-panel');

        // Скрываем все
        if (matrixAPanel) matrixAPanel.style.display = 'none';
        if (matrixBPanel) matrixBPanel.style.display = 'none';
        if (scalarPanel) scalarPanel.style.display = 'none';
        if (slaePanel) slaePanel.style.display = 'none';
        if (slaeVectorBPanel) slaeVectorBPanel.style.display = 'none';
        if (vectorAPanel) vectorAPanel.style.display = 'none';
        if (vectorsVectorBPanel) vectorsVectorBPanel.style.display = 'none';
        if (vectorCPanel) vectorCPanel.style.display = 'none';
        if (vectorScalarPanel) vectorScalarPanel.style.display = 'none';

        // Показываем выбранный режим
        if (mode === 'matrices') {
            if (matrixAPanel) matrixAPanel.style.display = 'block';
            if (matrixBPanel) matrixBPanel.style.display = 'block';
            if (scalarPanel) scalarPanel.style.display = 'block';
            updateMatricesPanelsVisibility();
        } else if (mode === 'slae') {
            if (matrixAPanel) matrixAPanel.style.display = 'block';
            if (slaePanel) slaePanel.style.display = 'block';
            if (slaeVectorBPanel) slaeVectorBPanel.style.display = 'block';
        } else if (mode === 'vectors') {
            if (vectorAPanel) vectorAPanel.style.display = 'block';
            if (vectorsVectorBPanel) vectorsVectorBPanel.style.display = 'block';
            if (vectorScalarPanel) vectorScalarPanel.style.display = 'block';
            updateVectorPanelsVisibility();
        }

        document.querySelectorAll('.mode-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        const activeBtn = document.getElementById(`mode-${mode}`);
        if (activeBtn) activeBtn.classList.add('active');
    }

    // ========== МАТРИЦЫ: СОХРАНЕНИЕ/ЗАГРУЗКА ==========
    function saveMatrixValuesToStorage(prefix) {
        const cells = document.querySelectorAll(`.matrix-cell[data-matrix="${prefix}"]`);
        const values = {};
        cells.forEach(cell => {
            const row = cell.dataset.row;
            const col = cell.dataset.col;
            if (!values[row]) values[row] = {};
            values[row][col] = cell.value;
        });
        localStorage.setItem(`matrix_${prefix}`, JSON.stringify(values));
        return values;
    }

    function loadMatrixValuesFromStorage(prefix) {
        const saved = localStorage.getItem(`matrix_${prefix}`);
        return saved ? JSON.parse(saved) : null;
    }

    function createMatrixHTML(rows, cols, prefix) {
        const savedValues = loadMatrixValuesFromStorage(prefix);
        let html = '<table class="matrix">';
        for (let i = 0; i < rows; i++) {
            html += '<tr>';
            for (let j = 0; j < cols; j++) {
                let value = '0';
                if (savedValues && savedValues[i] && savedValues[i][j] !== undefined) {
                    value = savedValues[i][j];
                }
                const cellId = `${prefix}_cell_${i}_${j}`;
                const cellName = `${prefix}[${i}][${j}]`;
                html += `<td><input type="number" class="matrix-cell"
                           id="${cellId}"
                           name="${cellName}"
                           data-matrix="${prefix}" data-row="${i}" data-col="${j}"
                           value="${value}" step="any"></td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }

    function updateMatrixA() {
        const rows = parseInt(document.getElementById('rows-a').value);
        const cols = parseInt(document.getElementById('cols-a').value);
        saveMatrixValuesToStorage('A');
        const matrixDiv = document.getElementById('matrix-a');
        if (matrixDiv) {
            matrixDiv.innerHTML = createMatrixHTML(rows, cols, 'A');
        }
        const sizeSpan = document.getElementById('matrix-a-size');
        if (sizeSpan) {
            sizeSpan.textContent = `(${rows}×${cols})`;
        }
        if (currentMode === 'slae') {
            updateSlaeVectorB();
        }
    }

    function updateMatrixB() {
        const rows = parseInt(document.getElementById('rows-b').value);
        const cols = parseInt(document.getElementById('cols-b').value);
        saveMatrixValuesToStorage('B');
        const matrixDiv = document.getElementById('matrix-b');
        if (matrixDiv) {
            matrixDiv.innerHTML = createMatrixHTML(rows, cols, 'B');
        }
        const sizeSpan = document.getElementById('matrix-b-size');
        if (sizeSpan) {
            sizeSpan.textContent = `(${rows}×${cols})`;
        }
    }

    function getMatrixValues(prefix) {
        const rows = prefix === 'A' ?
            parseInt(document.getElementById('rows-a').value) :
            parseInt(document.getElementById('rows-b').value);
        const cols = prefix === 'A' ?
            parseInt(document.getElementById('cols-a').value) :
            parseInt(document.getElementById('cols-b').value);
        const matrix = [];
        for (let i = 0; i < rows; i++) {
            const row = [];
            for (let j = 0; j < cols; j++) {
                const cell = document.querySelector(`.matrix-cell[data-matrix="${prefix}"][data-row="${i}"][data-col="${j}"]`);
                row.push(parseFloat(cell ? cell.value : 0) || 0);
            }
            matrix.push(row);
        }
        return matrix;
    }

    // ========== ВЕКТОРЫ: СОХРАНЕНИЕ И ЗАГРУЗКА ==========

    // Сохранение значений вектора в localStorage
    function saveVectorValuesToStorage(prefix) {
        const containerId = prefix === 'A' ? 'vector-a-input' :
                           (prefix === 'B' ? 'vectors-vector-b-input' : 'vector-c-input');
        const container = document.getElementById(containerId);
        if (!container) return;

        const cells = container.querySelectorAll('.vector-cell');
        const values = [];
        cells.forEach(cell => {
            values.push(cell.value);
        });
        localStorage.setItem(`vector_${prefix}`, JSON.stringify(values));
        return values;
    }

    function loadVectorValuesFromStorage(prefix) {
        const saved = localStorage.getItem(`vector_${prefix}`);
        return saved ? JSON.parse(saved) : null;
    }

    function getVectorValues(prefix) {
        let containerId;
        if (prefix === 'A') containerId = 'vector-a-input';
        else if (prefix === 'B') containerId = 'vectors-vector-b-input';
        else containerId = 'vector-c-input';

        const container = document.getElementById(containerId);
        if (!container) return [];

        const cells = container.querySelectorAll('.vector-cell');
        const vector = [];
        cells.forEach(cell => {
            vector.push(parseFloat(cell.value) || 0);
        });
        return vector;
    }

    function addDimensionControl(containerId, prefix) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // Удаляем старый контрол, если есть
        const oldControl = container.parentNode.querySelector('.vector-dimension-control');
        if (oldControl) oldControl.remove();

        // Загружаем сохранённую размерность
        const savedSize = localStorage.getItem(`vector_dim_${prefix}`);
        const defaultSize = savedSize ? parseInt(savedSize) : 3;

        const controlDiv = document.createElement('div');
        controlDiv.className = 'size-controls vector-dimension-control';
        controlDiv.innerHTML = `
            <label>Размерность вектора ${prefix}:
                <select class="vector-dimension" data-prefix="${prefix}">
                    <option value="1" ${defaultSize === 1 ? 'selected' : ''}>1</option>
                    <option value="2" ${defaultSize === 2 ? 'selected' : ''}>2</option>
                    <option value="3" ${defaultSize === 3 ? 'selected' : ''}>3</option>
                    <option value="4" ${defaultSize === 4 ? 'selected' : ''}>4</option>
                    <option value="5" ${defaultSize === 5 ? 'selected' : ''}>5</option>
                    <option value="6" ${defaultSize === 6 ? 'selected' : ''}>6</option>
                </select>
            </label>
        `;

        container.parentNode.insertBefore(controlDiv, container);

        // Создаём вектор с сохранённой размерностью
        createVectorInput(containerId, prefix, defaultSize);

        // Обработчик изменения размерности
        controlDiv.querySelector('.vector-dimension').addEventListener('change', function(e) {
            const newSize = parseInt(e.target.value);
            localStorage.setItem(`vector_dim_${prefix}`, newSize);
            createVectorInput(containerId, prefix, newSize);
        });
    }

    // ========== СЛАУ: ВЕКТОР ПРАВЫХ ЧАСТЕЙ ==========
    function createSlaeVectorHTML(size) {
        let html = '<div class="vector-container">';
        for (let i = 0; i < size; i++) {
            const cellId = `slae_vector_cell_${i}`;
            html += `
                <div class="vector-input">
                    <label for="${cellId}">b<sub>${i+1}</sub>:</label>
                    <input type="number" class="slae-vector-cell"
                           id="${cellId}"
                           data-index="${i}" value="0" step="any">
                </div>
            `;
        }
        html += '</div>';
        return html;
    }

    function updateSlaeVectorB() {
        const size = parseInt(document.getElementById('rows-a').value);
        const vectorDiv = document.getElementById('slae-vector-b');
        if (vectorDiv) {
            vectorDiv.innerHTML = createSlaeVectorHTML(size);
        }
    }

    function getSlaeVectorB() {
        const size = parseInt(document.getElementById('rows-a').value);
        const vector = [];
        for (let i = 0; i < size; i++) {
            const cell = document.querySelector(`.slae-vector-cell[data-index="${i}"]`);
            vector.push(parseFloat(cell ? cell.value : 0) || 0);
        }
        return vector;
    }

    // ========== ВЕКТОРЫ: ВВОД ==========
    function createVectorInput(containerId, prefix, size = 3) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.log(`Container ${containerId} not found`);
            return;
        }

        // Загружаем сохранённые значения
        const savedValues = loadVectorValuesFromStorage(prefix);
        console.log(`Loading vector ${prefix}:`, savedValues);

        let html = '<div class="vector-container">';
        for (let i = 0; i < size; i++) {
            let value = '0';
            if (savedValues && savedValues[i] !== undefined) {
                value = savedValues[i];
            }
            html += `
                <div class="vector-input">
                    <label>${prefix}<sub>${i+1}</sub>:</label>
                    <input type="number" step="any" class="vector-cell"
                           data-vector="${prefix}" data-index="${i}"
                           value="${value}">
                </div>
            `;
        }
        html += '</div>';
        container.innerHTML = html;

        // Добавляем обработчики для сохранения при изменении
        container.querySelectorAll('.vector-cell').forEach(cell => {
            cell.addEventListener('input', function() {
                saveVectorValuesToStorage(prefix);
            });
        });
    }

    function initAllVectors() {
        addDimensionControl('vector-a-input', 'A');
        addDimensionControl('vectors-vector-b-input', 'B');
        addDimensionControl('vector-c-input', 'C');
    }

    // ========== ЕДИНЫЙ ОБРАБОТЧИК ВЫЧИСЛЕНИЯ ==========
    document.getElementById('calculate-btn')?.addEventListener('click', function() {
        if (currentMode === 'matrices') {
            const request = { operation: currentOperation, matrixA: getMatrixValues('A') };
            const binaryOps = ['add', 'subtract', 'multiply'];
            if (binaryOps.includes(currentOperation)) {
                request.matrixB = getMatrixValues('B');
            }
            if (currentOperation === 'multiplyScalar') {
                request.scalar = parseFloat(document.getElementById('scalar').value) || 1;
            }
            sendRequest('/api/calculate', request);
        } else if (currentMode === 'slae') {
            const request = {
                operation: 'slae',
                matrixA: getMatrixValues('A'),
                vectorB: getSlaeVectorB(),
                slaeMethod: currentSlaeMethod
            };
            sendRequest('/api/calculate', request);
        } else if (currentMode === 'vectors') {
            const request = {
                operation: currentVectorOperation,
                vectorA: getVectorValues('A'),
                vectorB: getVectorValues('B'),
                vectorC: getVectorValues('C'),
                scalar: parseFloat(document.getElementById('vector-scalar').value) || 1
            };
            sendRequest('/api/vector/calculate', request);
        }
    });

    function sendRequest(url, request) {
        fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayResult(data.data);
                if (data.steps && data.steps.length > 0) {
                    displaySteps(data.steps);
                    document.getElementById('steps-panel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }
                document.getElementById('error').style.display = 'none';
            } else {
                displayError(data.error);
            }
        })
        .catch(error => {
            displayError('Ошибка соединения с сервером');
        });
    }

    // ========== ОТОБРАЖЕНИЕ РЕЗУЛЬТАТА ==========
    function displayResult(data) {
        const resultDiv = document.getElementById('result');
        if (!resultDiv) return;
        if (typeof data === 'number') {
            const displayValue = Number.isInteger(data) ? data : data.toFixed(4);
            resultDiv.innerHTML = `<h3>Результат: <span class="number-result">${displayValue}</span></h3>`;
        } else if (Array.isArray(data)) {
            if (data.length > 0 && !Array.isArray(data[0])) {
                let html = '<h3>Решение системы:</h3><div class="solution-vector">';
                for (let i = 0; i < data.length; i++) {
                    const val = data[i];
                    const displayVal = Number.isInteger(val) ? val : val.toFixed(4);
                    html += `<div class="solution-item">x<sub>${i+1}</sub> = ${displayVal}</div>`;
                }
                html += '</div>';
                resultDiv.innerHTML = html;
            } else {
                let html = '<h3>Результат:</h3><table class="matrix result-matrix">';
                for (let i = 0; i < data.length; i++) {
                    html += '<tr>';
                    for (let j = 0; j < data[i].length; j++) {
                        const val = data[i][j];
                        const displayVal = Number.isInteger(val) ? val : val.toFixed(4);
                        html += `<td>${displayVal}</td>`;
                    }
                    html += '</tr>';
                }
                html += '</table>';
                resultDiv.innerHTML = html;
            }
        }
    }

    function displaySteps(steps) {
        const stepsPanel = document.getElementById('steps-panel');
        const stepsList = document.getElementById('steps-list');
        if (!stepsPanel || !stepsList) return;
        let html = '';
        steps.forEach((step) => {
            html += '<div class="step-item">';
            if (step.text) {
                html += `<div class="step-text">${step.text}</div>`;
            }
            if (step.matrix && step.type) {
                if (step.type === 'matrix') {
                    html += formatMatrixHTML(step.matrix);
                } else if (step.type === 'vector') {
                    html += formatVectorHTML(step.matrix);
                } else if (step.type === 'augmented') {
                    html += formatAugmentedMatrixHTML(step.matrix);
                }
            }
            html += '</div>';
        });
        stepsList.innerHTML = html;
        stepsPanel.style.display = 'block';
    }

    function displayError(message) {
        document.getElementById('result').innerHTML = '';
        document.getElementById('steps-panel').style.display = 'none';
        const errorDiv = document.getElementById('error');
        if (errorDiv) {
            errorDiv.textContent = 'Ошибка: ' + message;
            errorDiv.style.display = 'block';
        }
    }

    function formatMatrixHTML(matrix) {
        if (!matrix || !matrix.length) return '';
        let html = '<table class="steps-matrix">';
        for (let i = 0; i < matrix.length; i++) {
            html += '<tr>';
            for (let j = 0; j < matrix[i].length; j++) {
                const val = matrix[i][j];
                const displayVal = Number.isInteger(val) ? val : val.toFixed(2);
                html += `<td class="steps-matrix-cell">${displayVal}<td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }

    function formatVectorHTML(vector) {
        if (!vector || !vector.length) return '';
        let html = '<div class="steps-vector"><table class="steps-matrix"><tr>';
        for (let i = 0; i < vector.length; i++) {
            const val = vector[i];
            const displayVal = Number.isInteger(val) ? val : val.toFixed(2);
            html += `<td class="steps-matrix-cell">${displayVal}</td>`;
        }
        html += '</tr></table></div>';
        return html;
    }

    function formatAugmentedMatrixHTML(matrix) {
        if (!matrix || !matrix.length) return '';
        const rows = matrix.length;
        const cols = matrix[0].length;
        let html = '<table class="steps-matrix augmented">';
        for (let i = 0; i < rows; i++) {
            html += '<td>';
            for (let j = 0; j < cols; j++) {
                const val = matrix[i][j];
                const displayVal = Number.isInteger(val) ? val : val.toFixed(2);
                const cellClass = (j === cols - 1) ? 'steps-matrix-cell last-col' : 'steps-matrix-cell';
                html += `<td class="${cellClass}">${displayVal}<td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }

    // ========== ИНИЦИАЛИЗАЦИЯ ==========
    console.log('Initializing matrices...');
    updateMatrixA();
    updateMatrixB();
    updateSlaeVectorB();
    updateMatricesPanelsVisibility();

    document.getElementById('resize-a')?.addEventListener('click', updateMatrixA);
    document.getElementById('resize-b')?.addEventListener('click', updateMatrixB);

    document.getElementById('rows-a')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixA();
    });
    document.getElementById('cols-a')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixA();
    });
    document.getElementById('rows-b')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixB();
    });
    document.getElementById('cols-b')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixB();
    });

    initAllVectors();

    document.getElementById('mode-matrices')?.addEventListener('click', () => switchMode('matrices'));
    document.getElementById('mode-slae')?.addEventListener('click', () => switchMode('slae'));
    document.getElementById('mode-vectors')?.addEventListener('click', () => switchMode('vectors'));

    switchMode('matrices');
});