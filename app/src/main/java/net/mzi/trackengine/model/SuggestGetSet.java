package net.mzi.trackengine.model;

public class SuggestGetSet {

	String id,name,SerialNo,ServiceItemNo,CustomerName,CustomerId,Mobile1,Email,EnterpriseId,ParentEnterpriseId,DepartmentId,DepartmentName,AssetCount;
	public SuggestGetSet(String id, String name,String SerialNo,String ServiceItemNo,String CustomerName,String CustomerId,String Mobile1,String Email,String EnterpriseId,String ParentEnterpriseId,String DepartmentId,String DepartmentName,String AssetCount){
		this.id=id;
        this.name=name;
        this.SerialNo=SerialNo;
        this.ServiceItemNo=ServiceItemNo;
        this.CustomerName=CustomerName;
        this.CustomerId=CustomerId;
        this.Mobile1=Mobile1;
        this.Email=Email;
        this.EnterpriseId=EnterpriseId;
        this.ParentEnterpriseId=ParentEnterpriseId;
        this.DepartmentId=DepartmentId;
        this.DepartmentName=DepartmentName;
        this.AssetCount=AssetCount;
	}
	public String getId() {
		return id;
	}

    public String SerialNo() {
        return SerialNo;
    }

    public String getAssetCount() {
        return AssetCount;
    }

    public String getDepartmentName() {
        return DepartmentName;
    }

    public String getDepartmentId() {
        return DepartmentId;
    }

    public String getParentEnterpriseId() {
        return ParentEnterpriseId;
    }

    public String getEnterpriseId() {
        return EnterpriseId;
    }

    public String getEmail() {
        return Email;
    }
    public String getMobile1() {
        return Mobile1;
    }

    public String getCustomerId() {
        return CustomerId;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public String getServiceItemNo() {
        return ServiceItemNo;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    /*public String getId() {
        return id;
    }*/

	/*public void setId(String id) {
		this.id = id;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
