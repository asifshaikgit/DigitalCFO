package com.idos.util;

import java.util.ArrayList;
import java.util.List;

import model.*;

import java.util.logging.Logger;
import java.util.logging.Level;

public class ListUtility {

	public static Logger log = Logger.getLogger("utility");

	// GIVE LIST OF ENTITY OBJECT TO BE REMOVED FROM DATABASE TABLE AND LIST OF
	// ENTITY OBJECTS TO BE CREATED IN THE DATABASE TABLE IN ORDER
	public static <entity extends BaseModel> List<List<entity>> getTransactionList(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((BranchVendors) newList).getEntityComparableParamId()
							.equals(((BranchVendors) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
						// ((BranchVendors) oldList).setOpeningBalance(((BranchVendors)
						// newList).getOpeningBalance());
						((BranchVendors) oldList)
								.setOriginalOpeningBalance(((BranchVendors) newList).getOriginalOpeningBalance());
						// ((BranchVendors) oldList)
						// .setOpeningBalanceAdvPaid(((BranchVendors)
						// newList).getOpeningBalanceAdvPaid());
						((BranchVendors) oldList).setOriginalOpeningBalanceAdvPaid(
								((BranchVendors) newList).getOriginalOpeningBalanceAdvPaid());
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getVendSpecificsTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		List<entity> updateCustEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((VendorSpecific) newList).getEntityComparableParamId()
							.equals(((VendorSpecific) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
						((VendorSpecific) oldList).setUnitPrice(((VendorSpecific) newList).getUnitPrice());
						((VendorSpecific) oldList).setRcmTaxRateVend(((VendorSpecific) newList).getRcmTaxRateVend());
						((VendorSpecific) oldList).setCessTaxRateVend(((VendorSpecific) newList).getCessTaxRateVend());
						((VendorSpecific) oldList)
								.setApplicableDateVendTax(((VendorSpecific) newList).getApplicableDateVendTax());
						updateEntityObjects.add(oldList);
						((VendorSpecific) oldList)
								.setDiscountPercentage(((VendorSpecific) newList).getDiscountPercentage());
						updateCustEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		returningListOfList.add(updateCustEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getProjPosQualificationTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((ProjectLabourPositionQualification) newList).getEntityComparableParamId()
							.equals(((ProjectLabourPositionQualification) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getProjectBranchesTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((ProjectBranches) newList).getEntityComparableParamId()
							.equals(((ProjectBranches) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getBranchSpecificsTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((BranchSpecifics) newList).getEntityComparableParamId()
							.equals(((BranchSpecifics) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getBranchSpecificsTransactionList1(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityList = new ArrayList<entity>();
		if (oldEntityList != null)
			log.log(Level.FINE, "Old entity list size = " + oldEntityList.size());
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		log.log(Level.FINE, "New entity list size = " + newEntityList.size());
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((BranchSpecifics) newList).getEntityComparableParamId()
							.equals(((BranchSpecifics) oldList).getEntityComparableParamId())) {
						flag = 1;
						((BranchSpecifics) oldList).setWalkinCustomerMaxDiscount(
								((BranchSpecifics) newList).getWalkinCustomerMaxDiscount());
						((BranchSpecifics) oldList).setOpeningBalance(((BranchSpecifics) newList).getOpeningBalance());
						((BranchSpecifics) oldList)
								.setInvOpeningBalance(((BranchSpecifics) newList).getInvOpeningBalance());
						((BranchSpecifics) oldList)
								.setInvOpeningBalRate(((BranchSpecifics) newList).getInvOpeningBalRate());
						((BranchSpecifics) oldList)
								.setInvOpeningBalUnits(((BranchSpecifics) newList).getInvOpeningBalUnits());
						clonedOldEntityList.remove(oldList);
						updateEntityList.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityList);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getWarehouseItemStockReorderLevelList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((WarehouseItemStockReorderLevel) newList).getEntityComparableParamId()
							.equals(((WarehouseItemStockReorderLevel) oldList).getEntityComparableParamId())) {
						flag = 1;
						((WarehouseItemStockReorderLevel) oldList).setReorderLevelAlertUser(
								((WarehouseItemStockReorderLevel) newList).getReorderLevelAlertUser());
						((WarehouseItemStockReorderLevel) oldList)
								.setReorderLevel(((WarehouseItemStockReorderLevel) newList).getReorderLevel());
						clonedOldEntityList.remove(oldList);
						updateEntityList.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityList);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getSpecificsTransactionPurposeTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((SpecificsTransactionPurpose) newList).getEntityComparableParamId()
							.equals(((SpecificsTransactionPurpose) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getSpecificsKnowledgeLibraryTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((SpecificsKnowledgeLibraryForBranch) newList).getEntityComparableParamId()
							.equals(((SpecificsKnowledgeLibraryForBranch) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserRoleTransactionList(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((UsersRoles) newList).getEntityComparableParamId()
							.equals(((UsersRoles) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserTransactionPurposeTransactionList(
			List<entity> oldEntityList, List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((UserTransactionPurpose) newList).getEntityComparableParamId()
							.equals(((UserTransactionPurpose) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserRightsInBranchList(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((UserRightInBranch) newList).getEntityComparableParamId()
							.equals(((UserRightInBranch) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserRightsInProjectList(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((UserRightForProject) newList).getEntityComparableParamId()
							.equals(((UserRightForProject) oldList).getEntityComparableParamId())) {
						flag = 1;
						clonedOldEntityList.remove(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserRightsInCOAList(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>(3);
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((UserRightSpecifics) newList).getEntityComparableParamId()
							.equals(((UserRightSpecifics) oldList).getEntityComparableParamId())) {
						flag = 1;
						((UserRightSpecifics) oldList).setAmount(((UserRightSpecifics) newList).getAmount());
						((UserRightSpecifics) oldList).setAmountTo(((UserRightSpecifics) newList).getAmountTo());
						clonedOldEntityList.remove(oldList);
						updateEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> gettGroupBnL(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((TravelGroupPermittedBoardingLodging) newList).getEntityComparableParamId()
							.equals(((TravelGroupPermittedBoardingLodging) oldList).getEntityComparableParamId())
							&& ((TravelGroupPermittedBoardingLodging) newList).getEntityComparableParamId1().equals(
									((TravelGroupPermittedBoardingLodging) oldList).getEntityComparableParamId1())) {
						flag = 1;
						((TravelGroupPermittedBoardingLodging) oldList).setMaxPermittedRoomCostPerNight(
								((TravelGroupPermittedBoardingLodging) newList).getMaxPermittedRoomCostPerNight());
						((TravelGroupPermittedBoardingLodging) oldList).setMaxPermittedFoodCostPerDay(
								((TravelGroupPermittedBoardingLodging) newList).getMaxPermittedFoodCostPerDay());
						clonedOldEntityList.remove(oldList);
						updateEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> geteGroupEIMC(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((ExpenseGroupExpenseItemMonetoryClaim) newList).getEntityComparableParamId()
							.equals(((ExpenseGroupExpenseItemMonetoryClaim) oldList).getEntityComparableParamId())
							&& ((ExpenseGroupExpenseItemMonetoryClaim) newList).getEntityComparableParamId1().equals(
									((ExpenseGroupExpenseItemMonetoryClaim) oldList).getEntityComparableParamId1())) {
						flag = 1;
						((ExpenseGroupExpenseItemMonetoryClaim) oldList).setMaximumPermittedAdvance(
								((ExpenseGroupExpenseItemMonetoryClaim) newList).getMaximumPermittedAdvance());
						((ExpenseGroupExpenseItemMonetoryClaim) oldList).setMonthlyMonetoryLimitForReimbursement(
								((ExpenseGroupExpenseItemMonetoryClaim) newList)
										.getMonthlyMonetoryLimitForReimbursement());
						clonedOldEntityList.remove(oldList);
						updateEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getTGDMKMATM(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((TravelGroupDistanceMilesKmsAllowedTravelMode) newList).getEntityComparableParamId1().equals(
							((TravelGroupDistanceMilesKmsAllowedTravelMode) oldList).getEntityComparableParamId1())
							&& ((TravelGroupDistanceMilesKmsAllowedTravelMode) newList).getEntityComparableParamId2()
									.equals(((TravelGroupDistanceMilesKmsAllowedTravelMode) oldList)
											.getEntityComparableParamId2())
							&& ((TravelGroupDistanceMilesKmsAllowedTravelMode) newList).getEntityComparableParamId3()
									.equals(((TravelGroupDistanceMilesKmsAllowedTravelMode) oldList)
											.getEntityComparableParamId3())) {
						flag = 1;
						((TravelGroupDistanceMilesKmsAllowedTravelMode) oldList).setOneWayFare(
								((TravelGroupDistanceMilesKmsAllowedTravelMode) newList).getOneWayFare());
						((TravelGroupDistanceMilesKmsAllowedTravelMode) oldList).setReturnFare(
								((TravelGroupDistanceMilesKmsAllowedTravelMode) newList).getReturnFare());
						clonedOldEntityList.remove(oldList);
						updateEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getSpecDUMLRFBnch(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>();
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newList : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldList : oldEntityList) {
					if (((SpecificsDocUploadMonetoryRuleForBranch) newList).getEntityComparableParamId1()
							.equals(((SpecificsDocUploadMonetoryRuleForBranch) oldList).getEntityComparableParamId1())
							&& ((SpecificsDocUploadMonetoryRuleForBranch) newList).getEntityComparableParamId2()
									.equals(((SpecificsDocUploadMonetoryRuleForBranch) oldList)
											.getEntityComparableParamId2())) {
						flag = 1;
						((SpecificsDocUploadMonetoryRuleForBranch) oldList).setMonetoryLimit(
								((SpecificsDocUploadMonetoryRuleForBranch) newList).getMonetoryLimit());
						clonedOldEntityList.remove(oldList);
						updateEntityObjects.add(oldList);
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newList);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}

	public static <entity extends BaseModel> List<List<entity>> getUserRightItems(List<entity> oldEntityList,
			List<entity> newEntityList) {
		List<List<entity>> returningListOfList = new ArrayList<List<entity>>(3);
		List<entity> newEntityObjects = new ArrayList<entity>();
		List<entity> clonedOldEntityList = new ArrayList<entity>();
		List<entity> updateEntityObjects = new ArrayList<entity>();
		if (oldEntityList != null) {
			clonedOldEntityList.addAll(oldEntityList);
		}
		for (entity newItem : newEntityList) {
			int flag = 0;
			if (oldEntityList != null) {
				for (entity oldItem : oldEntityList) {
					if (newItem instanceof UserRightCash) {
						if (((UserRightCash) newItem).getCash().getId()
								.equals(((UserRightCash) oldItem).getCash().getId())) {
							flag = 1;
							((UserRightCash) oldItem).setFromAmount(((UserRightCash) newItem).getFromAmount());
							((UserRightCash) oldItem).setToAmount(((UserRightCash) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					} else if (newItem instanceof UserRightBank) {
						if (((UserRightBank) newItem).getBank().getId()
								.equals(((UserRightBank) oldItem).getBank().getId())) {
							flag = 1;
							((UserRightBank) oldItem).setFromAmount(((UserRightBank) newItem).getFromAmount());
							((UserRightBank) oldItem).setToAmount(((UserRightBank) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					} else if (newItem instanceof UserRightCustomer) {
						if (((UserRightCustomer) newItem).getCustomer().getId()
								.equals(((UserRightCustomer) oldItem).getCustomer().getId())) {
							flag = 1;
							((UserRightCustomer) oldItem).setFromAmount(((UserRightCustomer) newItem).getFromAmount());
							((UserRightCustomer) oldItem).setToAmount(((UserRightCustomer) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					} else if (newItem instanceof UserRightTax) {
						if (((UserRightTax) newItem).getTax().getId()
								.equals(((UserRightTax) oldItem).getTax().getId())) {
							flag = 1;
							((UserRightTax) oldItem).setFromAmount(((UserRightTax) newItem).getFromAmount());
							((UserRightTax) oldItem).setToAmount(((UserRightTax) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					} else if (newItem instanceof UserRightInterBranch) {
						if (((UserRightInterBranch) newItem).getInterBranch().getId()
								.equals(((UserRightInterBranch) oldItem).getInterBranch().getId())) {
							flag = 1;
							((UserRightInterBranch) oldItem)
									.setFromAmount(((UserRightInterBranch) newItem).getFromAmount());
							((UserRightInterBranch) oldItem)
									.setToAmount(((UserRightInterBranch) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					} else if (newItem instanceof UserRightUsers) {
						if (((UserRightUsers) newItem).getOnUser().getId()
								.equals(((UserRightUsers) oldItem).getOnUser().getId())) {
							flag = 1;
							((UserRightUsers) oldItem).setFromAmount(((UserRightUsers) newItem).getFromAmount());
							((UserRightUsers) oldItem).setToAmount(((UserRightUsers) newItem).getToAmount());
							clonedOldEntityList.remove(oldItem);
							updateEntityObjects.add(oldItem);
						}
					}
				}
			}
			if (flag == 0) {
				newEntityObjects.add(newItem);
			}
		}
		if (oldEntityList != null) {
			oldEntityList.clear();
			oldEntityList.addAll(clonedOldEntityList);
		}
		returningListOfList.add(oldEntityList);
		returningListOfList.add(newEntityObjects);
		returningListOfList.add(updateEntityObjects);
		return returningListOfList;
	}
}
