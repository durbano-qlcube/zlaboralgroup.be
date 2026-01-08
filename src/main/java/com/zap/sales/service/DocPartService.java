package com.zap.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.zap.sales.entity.DocPartEntity;
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.entity.VentaPartEntity;
import com.zap.sales.exception.doc.DocNotFoundException;
import com.zap.sales.exception.doc.DocServiceException;
import com.zap.sales.vo.doc.DocPartVo;

@Stateless
public class DocPartService implements Serializable {

	private static final long serialVersionUID = 4358254814985062940L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DocPartService.class);
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	

	
	public DocPartVo create(DocPartVo docPartVo)
	{
		String TAG = "[docService - create]";
		
		if (docPartVo == null)
			throw new IllegalArgumentException(TAG + " >> 'docPartVo' can not be null");
	
		try {
			DocPartEntity docPartEntity = toDocPartEntity(docPartVo);
			docPartEntity.setFechaCreacion(Calendar.getInstance());
			em.persist(docPartEntity);
			return toDocPartVo(docPartEntity);

		} catch (Exception ex) {

			LOGGER.error(TAG + " - Error: ", ex);
			throw new DocServiceException(ex);
		}
	}
	
	public void delete(Integer idDoc) throws DocNotFoundException
	{
		String TAG = "[docService - delete idDoc:" + idDoc + "]";

		if (idDoc == null)
			throw new IllegalArgumentException(TAG + " >> 'idDoc' can not be null");

		try {
			DocPartEntity entity = em.find(DocPartEntity.class, idDoc);
			if (entity == null)
				throw new DocNotFoundException();

			em.remove(entity);

		} catch (DocNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	public DocPartVo load(Integer idDoc) throws DocNotFoundException {

		String TAG = "[docService - load idDoc:" + idDoc + "]";
		if (idDoc == null)
			throw new IllegalArgumentException(TAG + " >> 'iddoc' can not be null");

		try {

			DocPartEntity entity = em.find(DocPartEntity.class, idDoc);

			if (entity == null)
				return null;
			else
				return this.toDocPartVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	
	
	@SuppressWarnings("unchecked")
	public List<DocPartVo> loadAll() {
	    String TAG = "[docService - loadAll]";

	    try {
	        Query query = em.createNamedQuery("DocPartEntity.loadAll");

	        List<DocPartEntity> entityList = (List<DocPartEntity>) query.getResultList();
	        List<DocPartVo> result = new ArrayList<>();
	        
	        if (entityList != null && !entityList.isEmpty()) {
	            for (DocPartEntity source : entityList) {
	                result.add(toDocPartVo(source));
	            }
	        }

	        return result;

	    } catch (javax.persistence.NoResultException ex) {
	        return new ArrayList<>();

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new DocServiceException(ex);
	    }
	}
	
	
	
	public void update (DocPartVo DocPartVo, Boolean copyIfNull) throws DocNotFoundException
	{
		String TAG ="[DocService - update]";
		
		if (DocPartVo == null)
			throw new IllegalArgumentException(TAG +" >> 'DocPartVo' can not be null");
		
		if (DocPartVo.getIdDoc() == null)
			throw new IllegalArgumentException(TAG +" >> 'DocPartVo.getId()' can not be null");

		try{
			
			DocPartEntity entity = em.find(DocPartEntity.class, DocPartVo.getIdDoc());
			
			if(entity == null)
				throw new DocNotFoundException();
			
			this.toDocPartEntity(DocPartVo, entity, copyIfNull);
			entity.setFechaModificacion(Calendar.getInstance());
			em.merge(entity);
			
		}catch (DocNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[DocService - updateDoc] - Error: ",ex);
            throw new DocServiceException(ex);
		
		}
	}





	public List<DocPartVo> loadDocByIdVenta(Integer idVenta)
	{
		String TAG = "[DocService - loadDocByIdVenta idVenta:" + idVenta + "]";
		
		if (idVenta == null) {
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");
		}

		try {
			List<DocPartEntity> documentos = em.createNamedQuery("DocPartEntity.findByIdVenta", DocPartEntity.class)
			.setParameter("idVenta", idVenta)
			.getResultList();

			List<DocPartVo> docsVo = documentos.stream().map(this::toDocPartVo).collect(Collectors.toList());
			return documentos.stream().map(this::toDocPartVo).collect(Collectors.toList());

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}
	
	public List<DocPartVo> loadDocByIdEmpresa(Integer idEmpresa)
	{
		String TAG = "[DocService - loadDocByIdVenta idVenta:" + idEmpresa + "]";
		
		if (idEmpresa == null) {
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");
		}

		try {
			List<DocPartEntity> documentos = em.createNamedQuery("DocPartEntity.findByIdEmpresa", DocPartEntity.class)
			.setParameter("idEmpresa", idEmpresa)
			.getResultList();

			List<DocPartVo> docsVo = documentos.stream().map(this::toDocPartVo).collect(Collectors.toList());
			return documentos.stream().map(this::toDocPartVo).collect(Collectors.toList());

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	private DocPartVo toDocPartVo(DocPartEntity src)
	{
		DocPartVo dest = new DocPartVo();
		dest.setIdDoc(src.getIdDoc());
		dest.setDocType(src.getDocType());
		dest.setPath(src.getPath());
		dest.setFilenameOriginal(src.getFilenameOriginal());
		dest.setFilepath(src.getFilepath());
		dest.setUrl(src.getUrl());
		dest.setUser(src.getUser());
		dest.setFechaCreacion(src.getFechaCreacion());
		dest.setFechaModificacion(src.getFechaModificacion());
		
				
		if (src.getVentaPartEntity() != null) {
			dest.setIdVenta(src.getVentaPartEntity().getIdVenta());
		}

		return dest;
	}


	
	
	private void toDocPartEntity(DocPartVo source, DocPartEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdDoc()!=null)
			target.setIdDoc(source.getIdDoc());

		if(copyIfNull || source.getDocType()!=null)
			target.setDocType(source.getDocType());

		if(copyIfNull || source.getPath()!=null)
			target.setPath(source.getPath());
		
		if(copyIfNull || source.getFilenameOriginal()!=null)
			target.setFilenameOriginal(source.getFilenameOriginal());

	
		if(copyIfNull || source.getFilepath()!=null)
			target.setFilepath(source.getFilepath());

		
		if(copyIfNull || source.getUrl()!=null)
			target.setUrl(source.getUrl());

		
		if(copyIfNull || source.getUser()!=null)
			target.setUser(source.getUser());


	}

	
	
	
	private DocPartEntity toDocPartEntity(DocPartVo src)
	{
		DocPartEntity dest = new DocPartEntity();
		dest.setIdDoc(src.getIdDoc());
		dest.setDocType(src.getDocType());
		dest.setPath(src.getPath());
		dest.setFilenameOriginal(src.getFilenameOriginal());
		dest.setFilepath(src.getFilepath());
		dest.setUrl(src.getUrl());
		dest.setUser(src.getUser());
		
		

		if (src.getIdVenta() != null)
		{
			VentaPartEntity ventaPartEntity = new VentaPartEntity();
			ventaPartEntity.setIdVenta(src.getIdVenta());
			dest.setVentaPartEntity(ventaPartEntity);
		}

		return dest;
	}

	
	public Map<String, String> readPdfFields(InputStream pdfInputStream) throws IOException {
		Map<String, String> fieldsData = new HashMap<>();

		PdfReader reader = new PdfReader(pdfInputStream);
		AcroFields fields = reader.getAcroFields();
		Map<String, AcroFields.Item> formFields = fields.getFields();

		for (Map.Entry<String, AcroFields.Item> entry : formFields.entrySet()) {
			String fieldName = entry.getKey();
			String fieldValue = fields.getField(fieldName);
			fieldsData.put(fieldName, fieldValue);
		}

		reader.close();

		return fieldsData;
	}

	


	public void printFieldAppearanceStates(InputStream pdfInputStream, String fieldName) throws Exception {
	        PdfReader reader = null;
	        try {
	            reader = new PdfReader(pdfInputStream);
	            AcroFields fields = reader.getAcroFields();
	            Map<String, AcroFields.Item> formFields = fields.getFields();

	            if (formFields.containsKey(fieldName)) {
	                LOGGER.info("Field: " + fieldName);
	                for (String state : fields.getAppearanceStates(fieldName)) {
	                    LOGGER.info("Appearance state: " + state);
	                }
	            } else {
	                LOGGER.info("Field not found.");
	            }
	        } catch (Exception e) {
	            LOGGER.error("Error processing PDF", e);
	            throw e;
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (Exception e) {
	                    LOGGER.error("Error closing PdfReader", e);
	                }
	            }
	        }
	    }

	 	

	 	
	
}