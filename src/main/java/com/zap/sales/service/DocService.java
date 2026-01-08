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
import com.zap.sales.entity.DocEntity;
import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.exception.doc.DocNotFoundException;
import com.zap.sales.exception.doc.DocServiceException;
import com.zap.sales.vo.doc.DocVo;

@Stateless
public class DocService implements Serializable {

	private static final long serialVersionUID = 4358254814985062940L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DocService.class);
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	

	
	public DocVo create(DocVo docVo)
	{
		String TAG = "[docService - create]";
		
		if (docVo == null)
			throw new IllegalArgumentException(TAG + " >> 'docVo' can not be null");
	
		try {
			DocEntity docEntity = toDocEntity(docVo);
			docEntity.setFechaCreacion(Calendar.getInstance());
			em.persist(docEntity);
			return toDocVo(docEntity);

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
			DocEntity entity = em.find(DocEntity.class, idDoc);
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

	public DocVo load(Integer idDoc) throws DocNotFoundException {

		String TAG = "[docService - load idDoc:" + idDoc + "]";
		if (idDoc == null)
			throw new IllegalArgumentException(TAG + " >> 'iddoc' can not be null");

		try {

			DocEntity entity = em.find(DocEntity.class, idDoc);

			if (entity == null)
				return null;
			else
				return this.toDocVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	
	
	@SuppressWarnings("unchecked")
	public List<DocVo> loadAll() {
	    String TAG = "[docService - loadAll]";

	    try {
	        Query query = em.createNamedQuery("DocEntity.loadAll");

	        List<DocEntity> entityList = (List<DocEntity>) query.getResultList();
	        List<DocVo> result = new ArrayList<>();
	        
	        if (entityList != null && !entityList.isEmpty()) {
	            for (DocEntity source : entityList) {
	                result.add(toDocVo(source));
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
	
	
	
	public void update (DocVo DocVo, Boolean copyIfNull) throws DocNotFoundException
	{
		String TAG ="[DocService - update]";
		
		if (DocVo == null)
			throw new IllegalArgumentException(TAG +" >> 'DocVo' can not be null");
		
		if (DocVo.getIdDoc() == null)
			throw new IllegalArgumentException(TAG +" >> 'DocVo.getId()' can not be null");

		try{
			
			DocEntity entity = em.find(DocEntity.class, DocVo.getIdDoc());
			
			if(entity == null)
				throw new DocNotFoundException();
			
			this.toDocEntity(DocVo, entity, copyIfNull);
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





	public List<DocVo> loadDocByIdVenta(Integer idVenta)
	{
		String TAG = "[DocService - loadDocByIdVenta idVenta:" + idVenta + "]";
		
		if (idVenta == null) {
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");
		}

		try {
			List<DocEntity> documentos = em.createNamedQuery("DocEntity.findByIdVenta", DocEntity.class)
			.setParameter("idVenta", idVenta)
			.getResultList();

			List<DocVo> docsVo = documentos.stream().map(this::toDocVo).collect(Collectors.toList());
			return documentos.stream().map(this::toDocVo).collect(Collectors.toList());

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}
	
	public List<DocVo> loadDocByIdEmpresa(Integer idEmpresa)
	{
		String TAG = "[DocService - loadDocByIdVenta idVenta:" + idEmpresa + "]";
		
		if (idEmpresa == null) {
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");
		}

		try {
			List<DocEntity> documentos = em.createNamedQuery("DocEntity.findByIdEmpresa", DocEntity.class)
			.setParameter("idEmpresa", idEmpresa)
			.getResultList();

			List<DocVo> docsVo = documentos.stream().map(this::toDocVo).collect(Collectors.toList());
			return documentos.stream().map(this::toDocVo).collect(Collectors.toList());

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}








	private DocVo toDocVo(DocEntity src)
	{
		DocVo dest = new DocVo();
		dest.setIdDoc(src.getIdDoc());
		dest.setDocType(src.getDocType());
		dest.setPath(src.getPath());
		dest.setFilenameOriginal(src.getFilenameOriginal());
		dest.setFilepath(src.getFilepath());
		dest.setUrl(src.getUrl());
		dest.setUser(src.getUser());
		dest.setFechaCreacion(src.getFechaCreacion());
		dest.setFechaModificacion(src.getFechaModificacion());
		
		if (src.getEmpresaEntity() != null) {
			dest.setIdEmpresa(src.getEmpresaEntity().getIdEmpresa());
		}
		
		if (src.getVentaEntity() != null) {
			dest.setIdVenta(src.getVentaEntity().getIdVenta());
		}

		return dest;
	}


	
	
	private void toDocEntity(DocVo source, DocEntity target, Boolean copyIfNull)
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

	
	
	
	private DocEntity toDocEntity(DocVo src)
	{
		DocEntity dest = new DocEntity();
		dest.setIdDoc(src.getIdDoc());
		dest.setDocType(src.getDocType());
		dest.setPath(src.getPath());
		dest.setFilenameOriginal(src.getFilenameOriginal());
		dest.setFilepath(src.getFilepath());
		dest.setUrl(src.getUrl());
		dest.setUser(src.getUser());
		
		if (src.getIdEmpresa() != null)
		{
			EmpresaEntity empresaEntity = new EmpresaEntity();
			empresaEntity.setIdEmpresa(src.getIdEmpresa());
			dest.setEmpresaEntity(empresaEntity);
		}

		if (src.getIdVenta() != null)
		{
			VentaEntity ventaEntity = new VentaEntity();
			ventaEntity.setIdVenta(src.getIdVenta());
			dest.setVentaEntity(ventaEntity);
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