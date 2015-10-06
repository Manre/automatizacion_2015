/* ========================================================================
   * Copyright 2014 ejemplo
   *
   * Licensed under the MIT, The MIT License (MIT)
   * Copyright (c) 2014 ejemplo
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
   * ========================================================================
  
  
  Source generated by CrudMaker version 1.0.0.201411201032*/

package co.edu.uniandes.csw.ejemplo.carrito.persistence;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;


import co.edu.uniandes.csw.ejemplo.carrito.logic.dto.CarritoPageDTO;
import co.edu.uniandes.csw.ejemplo.carrito.logic.dto.CarritoDTO;
import co.edu.uniandes.csw.ejemplo.carrito.persistence.api.ICarritoPersistence;
import co.edu.uniandes.csw.ejemplo.carrito.persistence.entity.CarritoEntity;
import co.edu.uniandes.csw.ejemplo.carrito.persistence.converter.CarritoConverter;
import static co.edu.uniandes.csw.ejemplo.util._TestUtil.*;

@RunWith(Arquillian.class)
public class CarritoPersistenceTest {

	public static final String DEPLOY = "Prueba";

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class, DEPLOY + ".war")
				.addPackage(CarritoPersistence.class.getPackage())
				.addPackage(CarritoEntity.class.getPackage())
				.addPackage(ICarritoPersistence.class.getPackage())
                .addPackage(CarritoDTO.class.getPackage())
                .addPackage(CarritoConverter.class.getPackage())
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml");
	}

	@Inject
	private ICarritoPersistence carritoPersistence;

	@PersistenceContext
	private EntityManager em;

	@Inject
	UserTransaction utx;

	@Before
	public void configTest() {
		System.out.println("em: " + em);
		try {
			utx.begin();
			clearData();
			insertData();
			utx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				utx.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void clearData() {
		em.createQuery("delete from CarritoEntity").executeUpdate();
	}

	private List<CarritoEntity> data=new ArrayList<CarritoEntity>();

	private void insertData() {
		for(int i=0;i<3;i++){
			CarritoEntity entity=new CarritoEntity();
			entity.setName(generateRandom(String.class));
			entity.setClientId(generateRandom(Long.class));
			em.persist(entity);
			data.add(entity);
		}
	}
	
	@Test
	public void createCarritoTest(){
		CarritoDTO dto=new CarritoDTO();
		dto.setName(generateRandom(String.class));
		dto.setClientId(generateRandom(Long.class));
		
		CarritoDTO result=carritoPersistence.createCarrito(dto);
		
		Assert.assertNotNull(result);
		
		CarritoEntity entity=em.find(CarritoEntity.class, result.getId());
		
		Assert.assertEquals(dto.getName(), entity.getName());
		Assert.assertEquals(dto.getClientId(), entity.getClientId());
	}
	
	@Test
	public void getCarritosTest(){
		List<CarritoDTO> list=carritoPersistence.getCarritos();
		Assert.assertEquals(list.size(), data.size());
        for(CarritoDTO dto:list){
        	boolean found=false;
            for(CarritoEntity entity:data){
            	if(dto.getId().equals(entity.getId())){
                	found=true;
                }
            }
            Assert.assertTrue(found);
        }
	}
	
	@Test
	public void getCarritoTest(){
		CarritoEntity entity=data.get(0);
		CarritoDTO dto=carritoPersistence.getCarrito(entity.getId());
        Assert.assertNotNull(dto);
		Assert.assertEquals(entity.getName(), dto.getName());
		Assert.assertEquals(entity.getClientId(), dto.getClientId());
        
	}
	
	@Test
	public void deleteCarritoTest(){
		CarritoEntity entity=data.get(0);
		carritoPersistence.deleteCarrito(entity.getId());
        CarritoEntity deleted=em.find(CarritoEntity.class, entity.getId());
        Assert.assertNull(deleted);
	}
	
	@Test
	public void updateCarritoTest(){
		CarritoEntity entity=data.get(0);
		
		CarritoDTO dto=new CarritoDTO();
		dto.setId(entity.getId());
		dto.setName(generateRandom(String.class));
		dto.setClientId(generateRandom(Long.class));
		
		
		carritoPersistence.updateCarrito(dto);
		
		
		CarritoEntity resp=em.find(CarritoEntity.class, entity.getId());
		
		Assert.assertEquals(dto.getName(), resp.getName());	
		Assert.assertEquals(dto.getClientId(), resp.getClientId());	
	}
	
	@Test
	public void getCarritoPaginationTest(){
		//Page 1
		CarritoPageDTO dto1=carritoPersistence.getCarritos(1,2);
        Assert.assertNotNull(dto1);
        Assert.assertEquals(dto1.getRecords().size(),2);
        Assert.assertEquals(dto1.getTotalRecords().longValue(),3L);
        //Page 2
        CarritoPageDTO dto2=carritoPersistence.getCarritos(2,2);
        Assert.assertNotNull(dto2);
        Assert.assertEquals(dto2.getRecords().size(),1);
        Assert.assertEquals(dto2.getTotalRecords().longValue(),3L);
        
        for(CarritoDTO dto:dto1.getRecords()){
        	boolean found=false;	
            for(CarritoEntity entity:data){
            	if(dto.getId().equals(entity.getId())){
                	found=true;
                }
            }
            Assert.assertTrue(found);
        }
        
        for(CarritoDTO dto:dto2.getRecords()){
        	boolean found=false;
            for(CarritoEntity entity:data){
            	if(dto.getId().equals(entity.getId())){
                	found=true;
                }
            }
            Assert.assertTrue(found);
        }
	}
	
}