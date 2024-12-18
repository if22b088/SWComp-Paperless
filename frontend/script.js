document.getElementById('uploadButton').addEventListener('click', async () => {
    const fileInput = document.getElementById('documentUpload');


    const file = fileInput.files[0];

    if (file) {
        const formData = new FormData();
        formData.append('document', file);

        try {
            const response = await fetch('http://localhost:8081/documents', {
                method: 'POST',
                body: formData,
            });

            if (response.ok) {
                alert('Document uploaded successfully!');
                loadDocuments();
            } else {
                alert('Failed to upload document.');
            }
        } catch (error) {
            console.error('Error uploading document:', error);
        }
    } else {
        alert('Please select a file to upload.');
    }
});


document.getElementById('searchButton').addEventListener('click', async () => {
    const query = document.getElementById('searchQuery').value;

    if (!query) {
        alert('Please enter a search query.');
        return;
    }

    try {
        const response = await fetch(`http://localhost:8081/documents/search?query=${encodeURIComponent(query)}`);
        if (response.ok) {
            const searchResults = await response.json();
            const searchResultsTableBody = document.getElementById('searchResultsTableBody');
            searchResultsTableBody.innerHTML = '';

            if (searchResults.length === 0) {
                alert('No documents found matching the search criteria.');
                return;
            }

            searchResults.forEach(doc => {
                const row = document.createElement('tr');

                // Create cells for each attribute
                const idCell = document.createElement('td');
                idCell.textContent = doc.id;
                row.appendChild(idCell);

                const titleCell = document.createElement('td');
                titleCell.textContent = doc.title;
                row.appendChild(titleCell);

                const contentCell = document.createElement('td');
                contentCell.textContent = doc.content;
                row.appendChild(contentCell);

                const tagsCell = document.createElement('td');
                tagsCell.textContent = doc.tags.join(', ');
                row.appendChild(tagsCell);

                const dateCell = document.createElement('td');
                dateCell.textContent = new Date(doc.dateOfCreation).toLocaleString();
                row.appendChild(dateCell);

                searchResultsTableBody.appendChild(row);
            });

        } else {
            alert('Failed to fetch search results.');
        }
    } catch (error) {
        console.error('Error fetching search results:', error);
    }
});



// Get documents on page load
async function loadDocuments() {
    try {
        const response = await fetch('http://localhost:8081/documents');
        const documents = await response.json();
        const documentTableBody = document.getElementById('documentTableBody');
        documentTableBody.innerHTML = '';

        documents.forEach(doc => {
            const row = document.createElement('tr');

            // Create cells for each attribute
            const idCell = document.createElement('td');
            idCell.textContent = doc.id;
            row.appendChild(idCell);

            const titleCell = document.createElement('td');
            titleCell.textContent = doc.title;
            row.appendChild(titleCell);

            const contentCell = document.createElement('td');
            contentCell.textContent = doc.content;
            row.appendChild(contentCell);

            const tagsCell = document.createElement('td');
            tagsCell.textContent = doc.tags.join(', ');
            row.appendChild(tagsCell);

            const dateCell = document.createElement('td');
            dateCell.textContent = new Date(doc.dateOfCreation).toLocaleString();
            row.appendChild(dateCell);

            // create button and cell for the download button
            const downloadActionsCell = document.createElement('td');
            const downloadButton = document.createElement('button');
            downloadButton.textContent = 'Download';
            //store the doc.id and doc.title in data attribute
            downloadButton.dataset.docId = doc.id;
            downloadButton.dataset.docTitle = doc.title;
            downloadButton.onclick = (event) => {
                const docId = event.target.dataset.docId;
                const docTitle = event.target.dataset.docTitle;
                downloadDocument(docId, docTitle);
            };
            downloadActionsCell.appendChild(downloadButton);
            row.appendChild(downloadActionsCell);


            // create button and cell for the delete button
            const actionsCell = document.createElement('td');
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Delete';
            deleteButton.onclick = () => deleteDocument(doc.id);
            actionsCell.appendChild(deleteButton);
            row.appendChild(actionsCell);

            documentTableBody.appendChild(row);
        });

    } catch (error) {
        console.error('Error loading documents:', error);
    }
}

async function deleteDocument(id) {
    try {
        const response = await fetch(`http://localhost:8081/documents/${id}`, {
            method: 'DELETE',
        });

        if (response.ok) {
            loadDocuments();
        } else {
            alert('Failed to delete document.');
        }
    } catch (error) {
        console.error('Error deleting document:', error);
    }
}

function downloadDocument(docId, docTitle) {
    console.log('Downloading document with ID:', docId);
    fetch(`http://localhost:8081/documents/download/${docId}`)
        .then(response => {
            if (response.ok) {
                return response.blob();
            } else {
                throw new Error('Failed to download document');
            }
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = docTitle;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(error => console.error('Error downloading document:', error));
}

// Initial load
loadDocuments();
